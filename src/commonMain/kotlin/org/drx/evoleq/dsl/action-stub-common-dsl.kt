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
import org.drx.evoleq.evolution.stubs.ActionStub
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase

expect open class ActionStubConfiguration<I, Data> : UpdateStubConfiguration<Data> {

    class ActionStubConfigurationException(message: String?) : Exception

    var onInput: suspend CoroutineScope.(I, Data) -> Phase<Data>

    override fun configure(): ActionStub<I, Data>

    @EvoleqDsl
    override fun StubConfiguration<Data>.evolve(arrow: suspend CoroutineScope.(Data) -> Evolving<Data>)
    
    @EvoleqDsl
    fun ActionStubConfiguration<I, Data>.onInput(input: suspend CoroutineScope.(I, Data)-> Phase<Data>)

    @EvoleqDsl
    suspend fun ActionStubConfiguration<I,Data>.selfUpdate(data: Data, update: suspend CoroutineScope.(Data)->Data): Data


    @EvoleqDsl
    fun <J, E> ActionStubConfiguration<I, Data>.actionChild(id: ID, configuration: ActionStubConfiguration<J, E>.() -> Unit)
    
    @EvoleqDsl
    open fun <J, E> ActionStubConfiguration<I, Data>.actionChild(stub: ActionStub<J, E>)


}
@EvoleqDsl
expect fun <I, Data> actionStub(configuration: ActionStubConfiguration<I, Data>.()->Unit): ActionStub<I, Data>


@EvoleqDsl
expect fun <J, E> StubConfiguration<*>.actionChild(childId: ID,configuration: ActionStubConfiguration<J, E>.()->Unit)

@EvoleqDsl
expect fun <J, E> StubConfiguration<*>.actionChild(stub: ActionStub<J, E>)

@EvoleqDsl
expect fun <I, Data> ActionStub<I, Data>.configuration(): Pair<out StubConfiguration<*>,ActionStubConfiguration<I, Data>.()->Unit>