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
import org.drx.dynamics.ID
import org.drx.evoleq.evolution.Stub
import org.drx.evoleq.evolution.stubs.ActionStub
import org.drx.evoleq.evolution.stubs.Updated
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase

actual open class ActionStubConfiguration<I, Data> : UpdateStubConfiguration<Data>() {

    actual class ActionStubConfigurationException actual constructor(override val message: String?) : Exception(message)

    actual var onInput: suspend CoroutineScope.(I, Data) -> Phase<Data> = {_,data -> Phase.Stop(data)}

    //private val stub by Dynamic<ActionStub<I, Data>?>(null)

    //private val stubIsNotNull by stub.isNotNull()

    //class ParentIsNotNull

    //private val parentIsNotNull by stub.push(ParentIsNotNull::class) { it?.parent != null }

    actual override fun configure(): ActionStub<I, Data> = with(object : ActionStub<I, Data>(updateParent) {
        override val id: ID
            get() = this@ActionStubConfiguration.id

        override val parent: Stub<*>?
            get() = this@ActionStubConfiguration.parent

        override val stubs: HashMap<ID, Stub<*>>
            get() = this@ActionStubConfiguration.stubs

        override val onUpdate: suspend CoroutineScope.(Updated<Data>) -> Data
            get() = this@ActionStubConfiguration.onUpdate

        override val onStart: suspend CoroutineScope.(Data) -> Data
            get() = this@ActionStubConfiguration.onStart

        override val onStop: suspend CoroutineScope.(Data) -> Data
            get() = this@ActionStubConfiguration.onStop

        override val onInput: suspend CoroutineScope.(I, Data) -> Phase<Data>
            get() = this@ActionStubConfiguration.onInput

    }) stub@{
        configureChildren(this@stub)
        stub.value = this@stub
        this@stub
    }

    @EvoleqDsl
    actual override fun StubConfiguration<Data>.evolve(arrow: suspend CoroutineScope.(Data) -> Evolving<Data>) {
        throw ActionStubConfigurationException("evolve function is already defined")
    }

    @EvoleqDsl
    actual fun ActionStubConfiguration<I, Data>.onInput(input: suspend CoroutineScope.(I, Data)-> Phase<Data>) {
        this@ActionStubConfiguration.onInput= input
    }

    @EvoleqDsl
    actual suspend fun ActionStubConfiguration<I,Data>.selfUpdate(data: Data, update: suspend CoroutineScope.(Data)->Data): Data =
        Parallel{ onUpdate(Updated(this@ActionStubConfiguration.id, update(data))) }.get()


    @EvoleqDsl
    actual fun <J, E> ActionStubConfiguration<I, Data>.actionChild(id: ID, configuration: ActionStubConfiguration<J, E>.() -> Unit) {
        this@ActionStubConfiguration.childConfigurations[id] = Pair(ActionStubConfiguration<J, E>(),configuration as StubConfiguration<*>.()->Unit)
    }
    @EvoleqDsl
    actual open fun <J, E> ActionStubConfiguration<I, Data>.actionChild(stub: ActionStub<J, E>) { this@ActionStubConfiguration.actionChild(stub.id, stub.configuration().second) }


}
@EvoleqDsl
actual fun <I, Data> actionStub(configuration: ActionStubConfiguration<I, Data>.()->Unit): ActionStub<I, Data> = with(ActionStubConfiguration<I, Data>()) {
    configuration()
    configure()
}




    //configure(configuration) as ActionStub<I, Data>

@EvoleqDsl
actual fun <J, E> StubConfiguration<*>.actionChild(childId: ID,configuration: ActionStubConfiguration<J, E>.()->Unit) {
    childConfigurations[childId] = Pair(ActionStubConfiguration<J, E>(),configuration as StubConfiguration<*>.() -> Unit)
}

@EvoleqDsl
actual fun <J, E> StubConfiguration<*>.actionChild(stub: ActionStub<J, E>) { actionChild(stub.id, stub.configuration().second) }

@EvoleqDsl
actual fun <I, Data> ActionStub<I, Data>.configuration(): Pair<out StubConfiguration<*>,ActionStubConfiguration<I, Data>.()->Unit> =
    Pair<StubConfiguration<*>,ActionStubConfiguration<I, Data>.()->Unit>(ActionStubConfiguration<I, Data>()){
        id(this@configuration.id)
        onStart(this@configuration.onStart)
        onInput(this@configuration.onInput)
        onUpdate(this@configuration.onUpdate)
        onStop(this@configuration.onStop)
        stubs.putAll( this@configuration.stubs )
        when(this@configuration.parent) {
            null -> Unit
            else -> parent(this@configuration.parent!!)
        }
    }