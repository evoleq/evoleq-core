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
import org.drx.dynamics.ID
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.evolution.Stub
import org.drx.evoleq.evolution.find
import org.drx.evoleq.evolution.flows.process.SimpleProcessFlow
import org.drx.evoleq.evolving.Evolving
import org.evoleq.math.cat.suspend.morphism.by
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase


actual abstract class UpdateStub<Data> actual constructor(private val updateParent: suspend (Update<Data>)->Unit) : Stub<Data> {

    private class StopException: Exception()

    @ExperimentalCoroutinesApi
    internal val updateBroadcastChannel = BroadcastChannel<Update<Data>>(10_000)
    @ExperimentalCoroutinesApi
    internal open val updateReceiver = updateBroadcastChannel.openSubscription()

    @ExperimentalCoroutinesApi
    internal open val flow by lazy{ by(
        SimpleProcessFlow(
            onStart,
            {data -> with(updateReceiver.receive()){
                    with(morphism) {
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

                }
            },
            onStop
        )
    )}

    @ExperimentalCoroutinesApi
    override val morphism: suspend CoroutineScope.(Data) -> Evolving<Data> = {
            data -> flow(Phase.Start(data)) map { it.data }
    }

    actual abstract val onStart: suspend CoroutineScope.(Data)->Data

    actual abstract val onUpdate : suspend CoroutineScope.(Updated<Data>)->Data

    actual abstract val onStop: suspend CoroutineScope.(Data)->Data

    @ExperimentalCoroutinesApi
    @EvoleqDsl
    actual suspend fun stop() {
        update(id){
            throw StopException()
        }
    }
    @ExperimentalCoroutinesApi
    @EvoleqDsl
    actual suspend fun update(senderId:ID, update: suspend CoroutineScope.(Data)->Data) {
            updateBroadcastChannel.send(Update(senderId, update))
    }
    @ExperimentalCoroutinesApi
    @EvoleqDsl
    actual suspend fun <E> updateChild(childId: ID,  update: suspend CoroutineScope.(E)->E) {
        (find<E>(childId)!! as UpdateStub<E>).update(id, update)
    }
    @EvoleqDsl
    actual suspend fun <P> updateParent(update: suspend CoroutineScope.(Data)->Data) {
        updateParent(Update(id, update))
    }

    @ExperimentalCoroutinesApi
    @EvoleqDsl
    actual open fun closePorts() {
        updateBroadcastChannel.close()
    }
}

