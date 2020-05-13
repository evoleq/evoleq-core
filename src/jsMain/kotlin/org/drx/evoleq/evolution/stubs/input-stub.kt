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
package org.drx.evoleq.evolution.stubs

import kotlinx.coroutines.CoroutineScope
import org.drx.dynamics.DynamicArrayList
import org.drx.dynamics.onNext
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.evolution.Stub
import org.drx.evoleq.evolution.flows.process.SimpleProcessFlow
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.type.by
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase


abstract class InputStub<I,Data> : Stub<Data> {
    private val inputStack: DynamicArrayList<I> = DynamicArrayList<I>(arrayListOf())
    /*
    private val inputActor = CoroutineScope(Job()).actor<I>(capacity = 10_000) {
        for(input in channel) {
            inputStack.add(input)
        }
    }


     */
    private val flow by lazy{ by(SimpleProcessFlow(
        onStart,
        {data -> inputStack.onNext {
                input -> onInput(input,data)
        } },
        onStop
    ))}

    abstract val onStart: suspend CoroutineScope.(Data)->Data

    abstract val onInput: suspend CoroutineScope.(I,Data)->Phase<Data>

    private val onStopBase: suspend CoroutineScope.(Data)->Data = { data ->
        onStop(data)
    }
    abstract val onStop: suspend CoroutineScope.(Data)->Data

    @EvoleqDsl
    suspend fun input(input: I) {
       // inputActor.send(input)

        inputStack.add(input)
    }

    @EvoleqDsl
    open fun closePorts() {
        //inputActor.close()
    }
    override val morphism: suspend CoroutineScope.(Data) -> Evolving<Data> = {
        data -> flow(Phase.Start(data)) map { it.data }
    }
}
