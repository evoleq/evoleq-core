/**
 * Copyright (c) 2018-2020 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drx.evoleq.dsl

import kotlinx.coroutines.CoroutineScope
import org.drx.dynamics.Dynamic
import org.drx.dynamics.ID
import org.drx.dynamics.exec.blockUntil
import org.drx.dynamics.isNotNull
import org.drx.evoleq.evolution.Stub
import org.drx.evoleq.evolution.stubs.Update
import org.drx.evoleq.evolution.stubs.UpdateStub
import org.drx.evoleq.evolution.stubs.Updated
import org.drx.evoleq.evolution.stubs.data
import org.drx.evoleq.evolving.Evolving
import org.evoleq.math.cat.suspend.morphism.by

open class UpdateStubConfiguration<Data> : StubConfiguration<Data>() {

    private class UpdateStubConfigurationException(override val message: String?) : Exception(message)

    var updateParent: suspend (Update<Data>)->Unit = {}

    var onUpdate: suspend CoroutineScope.(Updated<Data>) -> Data = {updated -> updated.data}

    var onStart: suspend CoroutineScope.(Data)->Data = { data -> data }

    var onStop: suspend CoroutineScope.(Data)->Data = { data -> data }

    internal val stub by Dynamic<UpdateStub<Data>?>(null)

    internal val stubIsNotNull by stub.isNotNull()

    class ParentIsNotNull
    private val parentIsNotNull by stub.push(ParentIsNotNull::class){ it?.parent != null }

    override fun configure(): UpdateStub<Data> = with(object : UpdateStub<Data>(updateParent){
        override val id: ID
            get() = this@UpdateStubConfiguration.id

        override val parent: Stub<*>?
            get() = this@UpdateStubConfiguration.parent

        override val stubs: HashMap<ID, Stub<*>>
            get() = this@UpdateStubConfiguration.stubs

        override val onUpdate: suspend CoroutineScope.(Updated<Data>) -> Data
            get() = this@UpdateStubConfiguration.onUpdate

        override val onStart: suspend CoroutineScope.(Data) -> Data
            get() = this@UpdateStubConfiguration.onStart

        override val onStop: suspend CoroutineScope.(Data) -> Data
            get() = this@UpdateStubConfiguration.onStop

    }) stub@{
        configureChildren(this@stub)
        stub.value = this@stub
        this@stub
    }

    @EvoleqDsl
    override fun StubConfiguration<Data>.evolve(arrow: suspend CoroutineScope.(Data) -> Evolving<Data>) {
        throw UpdateStubConfigurationException("evolve function is already defined")
    }

    @EvoleqDsl
    fun UpdateStubConfiguration<Data>.onStart(onStart: suspend CoroutineScope.(Data)->Data){
        this@UpdateStubConfiguration.onStart = onStart
    }

    @EvoleqDsl
    fun UpdateStubConfiguration<Data>.onStop(onStop: suspend CoroutineScope.(Data)->Data){
        this@UpdateStubConfiguration.onStop = onStop
    }



    @EvoleqDsl
    fun UpdateStubConfiguration<Data>.onUpdate(update: suspend CoroutineScope.(Updated<Data>) -> Data) {
        this@UpdateStubConfiguration.onUpdate = update
    }

    @EvoleqDsl
    fun <P> UpdateStubConfiguration<Data>.updateParentBy(setter:suspend  (suspend CoroutineScope.(Data) -> Data)->(suspend CoroutineScope.(P)->P)) {

            this@UpdateStubConfiguration.updateParent = {
                update: Update<Data> -> parentalUpdateStub<P>().update(id, setter(by(update.data())))
            }
    }

    @EvoleqDsl
    suspend fun <P> UpdateStubConfiguration<Data>.updateParent(update: suspend CoroutineScope.(Data)->Data) {
            stub().updateParent<P>(update)
    }

    @EvoleqDsl
    suspend fun <E> UpdateStubConfiguration<Data>.updateChild(childId: ID,  update: suspend CoroutineScope.(E)->E) {
            stub().updateChild(childId,update)
    }


    @EvoleqDsl
    fun <E> UpdateStubConfiguration<Data>.updatableChild(id: ID, configuration: UpdateStubConfiguration<E>.() -> Unit) {
        childConfigurations[id] = Pair(UpdateStubConfiguration<E>(),configuration as StubConfiguration<*>.()->Unit)
    }
    @EvoleqDsl
    open fun <E> UpdateStubConfiguration<Data>.updatableChild(stub: UpdateStub<E>) { updatableChild(stub.id, stub.configuration().second) }


    suspend fun UpdateStubConfiguration<Data>.stub(): UpdateStub<Data> {
        blockUntil(stubIsNotNull){
            it
        }
        return stub.value!!
    }

    @Suppress("unchecked_cast")
    private suspend fun <P> parentalUpdateStub():  UpdateStub<P> {
        blockUntil(parentIsNotNull){it}
        return with(parent!!){
            require(this is UpdateStub)
            this as UpdateStub<P>
        }
    }
}

@EvoleqDsl
fun <Data> updateStub(configuration: UpdateStubConfiguration<Data>.()->Unit): UpdateStub<Data> = with(UpdateStubConfiguration<Data>()) {
    configuration()
    configure()
}//configure(configuration) as UpdateStub<Data>

@EvoleqDsl
fun <E> StubConfiguration<*>.updatableChild(childId: ID,configuration: UpdateStubConfiguration<E>.()->Unit) {
    childConfigurations[childId] = Pair(UpdateStubConfiguration<E>(),configuration as StubConfiguration<*>.() -> Unit)
}

@EvoleqDsl
fun <E> StubConfiguration<*>.updatableChild(stub: UpdateStub<E>) { updatableChild(stub.id, stub.configuration().second) }

@EvoleqDsl
fun <Data> UpdateStub<Data>.configuration(): Pair<out StubConfiguration<*>,UpdateStubConfiguration<Data>.()->Unit> =
    Pair<StubConfiguration<*>,UpdateStubConfiguration<Data>.()->Unit>(UpdateStubConfiguration<Data>()){
        id(this@configuration.id)
        onStart(this@configuration.onStart)
        onUpdate(this@configuration.onUpdate)
        onStop(this@configuration.onStop)
        stubs.putAll( this@configuration.stubs )
        when(this@configuration.parent) {
            null -> Unit
            else -> parent(this@configuration.parent!!)
        }
    }