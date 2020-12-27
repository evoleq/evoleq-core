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
import org.drx.evoleq.evolution.stubs.InputStub
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase

actual class InputStubConfiguration<I, Data> : StubConfiguration<Data>() {

    actual class InputStubConfigurationException actual constructor(override val message: String?) : Exception(message)

    actual var onStart: suspend CoroutineScope.(Data)->Data = {data -> data}

    private lateinit var onInput: suspend CoroutineScope.(I, Data)-> Phase<Data>

    private var onStop: suspend CoroutineScope.(Data)->Data = {data -> data}

    actual override fun configure(): InputStub<I, Data> = with(object : InputStub<I, Data>(){
        override val id: ID
            get() = this@InputStubConfiguration.id
        override val parent: Stub<*>?
            get() = this@InputStubConfiguration.parent
        override val stubs: HashMap<ID, Stub<*>>
            get() = this@InputStubConfiguration.stubs
        override val onStart: suspend CoroutineScope.(Data) -> Data
            get() = this@InputStubConfiguration.onStart
        override val onInput: suspend CoroutineScope.(I, Data) -> Phase<Data>
            get() = this@InputStubConfiguration.onInput
        override val onStop: suspend CoroutineScope.(Data) -> Data
            get() = this@InputStubConfiguration.onStop


    }) {
        configureChildren(this)
        this
    }

    @EvoleqDsl
    actual override fun StubConfiguration<Data>.evolve(arrow: suspend CoroutineScope.(Data) -> Evolving<Data>) {
        throw InputStubConfigurationException("evolve function is already defined")
    }

    @EvoleqDsl
    actual fun InputStubConfiguration<I, Data>.onInput(input: suspend CoroutineScope.(I, Data)-> Phase<Data>) {
        this@InputStubConfiguration.onInput= input
    }

    @EvoleqDsl
    actual fun InputStubConfiguration<I, Data>.onStart(onStart: suspend CoroutineScope.(Data)->Data){
        this@InputStubConfiguration.onStart = onStart
    }

    @EvoleqDsl
    actual fun InputStubConfiguration<I, Data>.onStop(onStop: suspend CoroutineScope.(Data)->Data){
        this@InputStubConfiguration.onStop = onStop
    }

    @EvoleqDsl
    actual fun <J, E> InputStubConfiguration<I, Data>.inputChild(id: ID, configuration: InputStubConfiguration<J, E>.() -> Unit) {
        this@InputStubConfiguration.childConfigurations[id] = Pair(InputStubConfiguration<J, E>(),configuration as StubConfiguration<*>.()->Unit)
    }
    @EvoleqDsl
    actual open fun <J, E> InputStubConfiguration<I, Data>.inputChild(stub: InputStub<J, E>) { this@InputStubConfiguration.inputChild(stub.id, stub.configuration().second) }


}

@EvoleqDsl
actual fun <I, Data> inputStub(configuration: InputStubConfiguration<I, Data>.()->Unit): InputStub<I, Data> = with(InputStubConfiguration<I, Data>()) {
    configuration()
    configure()
}//= configure(configuration) as InputStub<I, Data>

@EvoleqDsl
actual fun <J, E> StubConfiguration<*>.inputChild(childId: ID,configuration: InputStubConfiguration<J, E>.()->Unit) {
    childConfigurations[childId] = Pair(InputStubConfiguration<J, E>(),configuration as StubConfiguration<*>.() -> Unit)
}

@EvoleqDsl
actual fun <J, E> StubConfiguration<*>.inputChild(stub: InputStub<J, E>) { inputChild(stub.id, stub.configuration().second) }


@EvoleqDsl
actual fun <I, Data> InputStub<I, Data>.configuration(): Pair<out StubConfiguration<*>,InputStubConfiguration<I, Data>.()->Unit> =
    Pair<StubConfiguration<*>,InputStubConfiguration<I, Data>.()->Unit>(InputStubConfiguration<I, Data>()){
        id(this@configuration.id)
        onStart(this@configuration.onStart)
        onInput(this@configuration.onInput)
        onStop(this@configuration.onStop)
        stubs.putAll( this@configuration.stubs )
        when(this@configuration.parent) {
            null -> Unit
            else -> parent(this@configuration.parent!!)
        }
    }