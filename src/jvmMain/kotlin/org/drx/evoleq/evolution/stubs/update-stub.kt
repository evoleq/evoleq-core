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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.actor
import org.drx.dynamics.DynamicArrayList
import org.drx.dynamics.ID
import org.drx.dynamics.onNext
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.evolution.Stub
import org.drx.evoleq.evolution.find
import org.drx.evoleq.evolution.flows.process.SimpleProcessFlow
import org.drx.evoleq.evolving.Evolving
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase

data class Updated<Data> (val senderId : ID, val data: Data)

interface Update<Data> : ScopedSuspended<Data, Updated<Data>>

suspend fun <Data> Update<Data>.data(): ScopedSuspended<Data,Data> = ScopedSuspended { data -> by(this@data)(this,data).data }

@Suppress("FunctionName")
fun<Data> Update(senderId: ID, update: suspend CoroutineScope.(Data) -> Data) = object :
    Update<Data> {
    override val morphism: suspend CoroutineScope.(Data) -> Updated<Data> = {
        data ->
        Updated(senderId, update(data))
    }
}

abstract class UpdateStub<Data>(private val updateParent: suspend (Update<Data>)->Unit = {}) : Stub<Data> {

    private class StopException: Exception()

    private val updateStack by DynamicArrayList<Update<Data>>(arrayListOf())
    private val updateActor = CoroutineScope(Job()).actor<Update<Data>>(capacity = 10_000) {
        for(update in channel) {
            updateStack.add(update)
        }
    }

    private val flow by lazy{ by(
        SimpleProcessFlow(
            onStart,
            {data -> updateStack.onNext { update ->
                with(update.morphism) {
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
            } },
            onStop
        )
    )}

    override val morphism: suspend CoroutineScope.(Data) -> Evolving<Data> = {
            data -> flow(Phase.Start(data)) map { it.data }
    }

    abstract val onStart: suspend CoroutineScope.(Data)->Data

    abstract val onUpdate : suspend CoroutineScope.(Updated<Data>)->Data

    abstract val onStop: suspend CoroutineScope.(Data)->Data

    @EvoleqDsl
    suspend fun stop() {
        update(id){
            throw StopException()
        }
    }
    @EvoleqDsl
    suspend fun update(senderId:ID, update: suspend CoroutineScope.(Data)->Data) {
            updateActor.send(Update(senderId, update))
    }
    @EvoleqDsl
    suspend fun <E> updateChild(childId: ID,  update: suspend CoroutineScope.(E)->E) {
        (find<E>(childId)!! as UpdateStub<E>).update(id, update)
    }
    @EvoleqDsl
    suspend fun <P> updateParent(update: suspend CoroutineScope.(Data)->Data) {
        updateParent(Update(id, update))
    }

    @EvoleqDsl
    open fun closePorts() {
        updateActor.close()
    }
}