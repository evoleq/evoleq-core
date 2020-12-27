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
import org.drx.evoleq.evolution.stubs.InputStub
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase

expect class InputStubConfiguration<I, Data> : StubConfiguration<Data> {

    class InputStubConfigurationException( message: String?) : Exception

    var onStart: suspend CoroutineScope.(Data)->Data// = {data -> data}
    
    override fun configure(): InputStub<I, Data>
    
    @EvoleqDsl
    override fun StubConfiguration<Data>.evolve(arrow: suspend CoroutineScope.(Data) -> Evolving<Data>)

    @EvoleqDsl
    fun InputStubConfiguration<I, Data>.onInput(input: suspend CoroutineScope.(I, Data)-> Phase<Data>)

    @EvoleqDsl
    fun InputStubConfiguration<I, Data>.onStart(onStart: suspend CoroutineScope.(Data)->Data)

    @EvoleqDsl
    fun InputStubConfiguration<I, Data>.onStop(onStop: suspend CoroutineScope.(Data)->Data)

    @EvoleqDsl
    fun <J, E> InputStubConfiguration<I, Data>.inputChild(id: ID, configuration: InputStubConfiguration<J, E>.() -> Unit)
    @EvoleqDsl
    open fun <J, E> InputStubConfiguration<I, Data>.inputChild(stub: InputStub<J, E>)

}

@EvoleqDsl
expect fun <I, Data> inputStub(configuration: InputStubConfiguration<I, Data>.()->Unit): InputStub<I, Data>

@EvoleqDsl
expect fun <J, E> StubConfiguration<*>.inputChild(childId: ID,configuration: InputStubConfiguration<J, E>.()->Unit)

@EvoleqDsl
expect fun <J, E> StubConfiguration<*>.inputChild(stub: InputStub<J, E>)

@EvoleqDsl
expect fun <I, Data> InputStub<I, Data>.configuration(): Pair<out StubConfiguration<*>,InputStubConfiguration<I, Data>.()->Unit>