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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

external fun setTimeout(function: () -> Unit, delay: Long)

suspend fun delay(ms: Long): Unit = suspendCoroutine { continuation ->
    setTimeout({
        println("timeout")
        continuation.resume(Unit)
    }, ms)
}

actual open class UpdateStubConfiguration<Data> : StubConfiguration<Data>() {

    actual class UpdateStubConfigurationException actual constructor(override val message: String?) : Exception(message)

    actual var updateParent: suspend (Update<Data>)->Unit = {}

    actual var onUpdate: suspend CoroutineScope.(Updated<Data>) -> Data = { updated -> updated.data}

    actual var onStart: suspend CoroutineScope.(Data)->Data = { data -> data }

    actual var onStop: suspend CoroutineScope.(Data)->Data = { data -> data }

    internal actual val stub by Dynamic<UpdateStub<Data>?>(null)

    private lateinit var stubIsNotNullInner: Dynamic<Boolean>
    internal val stubIsNotNull by lazy {
        GlobalScope.launch {
            stubIsNotNullInner = stub.isNotNull()
        }
        while(!::stubIsNotNullInner.isInitialized) {
            setTimeout({},1)
        }
        stubIsNotNullInner
    }

    class ParentIsNotNull
    private lateinit var parentIsNotNullInner: Dynamic<Boolean>
    private val parentIsNotNull by lazy{
        GlobalScope.launch {
            parentIsNotNullInner = stub.push(ParentIsNotNull::class){ it?.parent != null }
        }
        while(!::parentIsNotNullInner.isInitialized) {
            setTimeout({}, 1)
        }
        parentIsNotNullInner
    }

    actual override fun configure(): UpdateStub<Data> = with(object : UpdateStub<Data>(updateParent){
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
    actual override fun StubConfiguration<Data>.evolve(arrow: suspend CoroutineScope.(Data) -> Evolving<Data>) {
        throw UpdateStubConfigurationException("evolve function is already defined")
    }

    @EvoleqDsl
    actual fun UpdateStubConfiguration<Data>.onStart(onStart: suspend CoroutineScope.(Data)->Data){
        this@UpdateStubConfiguration.onStart = onStart
    }

    @EvoleqDsl
    actual fun UpdateStubConfiguration<Data>.onStop(onStop: suspend CoroutineScope.(Data)->Data){
        this@UpdateStubConfiguration.onStop = onStop
    }



    @EvoleqDsl
    actual fun UpdateStubConfiguration<Data>.onUpdate(update: suspend CoroutineScope.(Updated<Data>) -> Data) {
        this@UpdateStubConfiguration.onUpdate = update
    }

    @ExperimentalCoroutinesApi
    @EvoleqDsl
    actual fun <P> UpdateStubConfiguration<Data>.updateParentBy(setter:suspend  (suspend CoroutineScope.(Data) -> Data)->(suspend CoroutineScope.(P)->P)) {

            this@UpdateStubConfiguration.updateParent = {
                update: Update<Data> -> parentalUpdateStub<P>().update(id, setter(by(update.data())))
            }
    }

    @EvoleqDsl
    actual suspend fun <P> UpdateStubConfiguration<Data>.updateParent(update: suspend CoroutineScope.(Data)->Data) {
            stub().updateParent<P>(update)
    }

    @ExperimentalCoroutinesApi
    @EvoleqDsl
    actual suspend fun <E> UpdateStubConfiguration<Data>.updateChild(childId: ID,  update: suspend CoroutineScope.(E)->E) {
            stub().updateChild(childId,update)
    }


    @EvoleqDsl
    actual fun <E> UpdateStubConfiguration<Data>.updatableChild(id: ID, configuration: UpdateStubConfiguration<E>.() -> Unit) {
        childConfigurations[id] = Pair(UpdateStubConfiguration<E>(),configuration as StubConfiguration<*>.()->Unit)
    }
    @EvoleqDsl
    actual open fun <E> UpdateStubConfiguration<Data>.updatableChild(stub: UpdateStub<E>) { updatableChild(stub.id, stub.configuration().second) }


    actual suspend fun UpdateStubConfiguration<Data>.stub(): UpdateStub<Data> {
        blockUntil(stubIsNotNull){
            it
        }
        return stub.value!!
    }

    @Suppress("unchecked_cast")
    actual suspend fun <P> parentalUpdateStub():  UpdateStub<P> {
        blockUntil(parentIsNotNull){it}
        return with(parent!!){
            require(this is UpdateStub)
            this as UpdateStub<P>
        }
    }
}

@EvoleqDsl
actual fun <Data> updateStub(configuration: UpdateStubConfiguration<Data>.()->Unit): UpdateStub<Data> = with(UpdateStubConfiguration<Data>()) {
    configuration()
    configure()
}//configure(configuration) as UpdateStub<Data>

@EvoleqDsl
actual fun <E> StubConfiguration<*>.updatableChild(childId: ID, configuration: UpdateStubConfiguration<E>.()->Unit) {
    childConfigurations[childId] = Pair(UpdateStubConfiguration<E>(),configuration as StubConfiguration<*>.() -> Unit)
}



@EvoleqDsl
actual fun <Data> UpdateStub<Data>.configuration(): Pair<out StubConfiguration<*>,UpdateStubConfiguration<Data>.()->Unit> =
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