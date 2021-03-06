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
import kotlinx.coroutines.delay
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.evolution.flows.process.SimpleProcessFlow
import org.drx.evoleq.evolving.Evolving
import org.evoleq.math.cat.suspend.morphism.by
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase


actual abstract class ActionStub<I, Data> actual constructor(
    private val updateParent: suspend (Update<Data>)->Unit
) : UpdateStub<Data>(updateParent) {

    private class StopException: Exception()

    @ExperimentalCoroutinesApi
    val inputBroadcastChannel  = BroadcastChannel<I>(10_000)
    @ExperimentalCoroutinesApi
    val inputReceiver = inputBroadcastChannel.openSubscription()

    @ExperimentalCoroutinesApi
    override val flow by lazy{ by(
        SimpleProcessFlow(
            onStart,
            {data ->
                while(updateReceiver.isEmpty && inputReceiver.isEmpty) {
                    delay(1)
                }
                if (!updateReceiver.isEmpty) {
                    with(by(updateReceiver.receive())) {
                        try {
                            if (this(data).data != data) {
                                Phase.Wait(onUpdate(this(data)))
                            } else {
                                Phase.Wait(data)
                            }
                        } catch (exception: StopException) {
                            Phase.Stop(data)
                        }
                    }
                } else if (!inputReceiver.isEmpty) {
                    onInput(inputReceiver.receive(), data)
                } else {
                    Phase.Wait(data)
                }
            },
            onStop
        )
    )}

    @ExperimentalCoroutinesApi
    override val morphism: suspend CoroutineScope.(Data) -> Evolving<Data> = {
            data -> flow(Phase.Start(data)) map { it.data }
    }

    abstract val onInput: suspend CoroutineScope.(I,Data)->Phase<Data>

    @ExperimentalCoroutinesApi
    @EvoleqDsl
    actual suspend fun input(input: I) {
        inputBroadcastChannel.send(input)
    }

    @ExperimentalCoroutinesApi
    @EvoleqDsl
    override  fun closePorts() {
        updateBroadcastChannel.close()
        inputBroadcastChannel.close()
    }
}

