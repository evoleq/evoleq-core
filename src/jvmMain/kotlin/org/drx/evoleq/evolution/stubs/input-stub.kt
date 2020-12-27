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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.evolution.Stub
import org.drx.evoleq.evolution.flows.process.SimpleProcessFlow
import org.drx.evoleq.evolving.Evolving
import org.evoleq.math.cat.suspend.morphism.by
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase


actual abstract class InputStub<I,Data> : Stub<Data> {

    @ExperimentalCoroutinesApi
    val inputBroadcastChannel  = BroadcastChannel<I>(10_000)

    @ExperimentalCoroutinesApi
    val inputReceiver = inputBroadcastChannel.openSubscription()

    @ExperimentalCoroutinesApi
    private val flow by lazy{ by(SimpleProcessFlow(
        onStart,
        {data ->  onInput(inputReceiver.receive(),data)},
        onStop
    ))}

    actual abstract val onStart: suspend CoroutineScope.(Data)->Data

    actual abstract val onInput: suspend CoroutineScope.(I,Data)->Phase<Data>

    private val onStopBase: suspend CoroutineScope.(Data)->Data = { data ->
        onStop(data)
    }
    actual abstract val onStop: suspend CoroutineScope.(Data)->Data

    @ExperimentalCoroutinesApi
    @EvoleqDsl
    actual suspend fun input(input: I) {
        inputBroadcastChannel.send(input)
    }

    @ExperimentalCoroutinesApi
    @EvoleqDsl
    actual open fun closePorts() {
        inputBroadcastChannel.close()
    }
    @ExperimentalCoroutinesApi
    override val morphism: suspend CoroutineScope.(Data) -> Evolving<Data> = {
        data -> flow(Phase.Start(data)) map { it.data }
    }
}
