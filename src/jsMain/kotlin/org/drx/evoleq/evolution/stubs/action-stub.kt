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
import org.drx.dynamics.and
import org.drx.dynamics.exec.blockUntil
import org.drx.dynamics.not
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.evolution.flows.process.SimpleProcessFlow
import org.drx.evoleq.evolving.Evolving
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase


abstract class ActionStub<I, Data>(private val updateParent: suspend (Update<Data>)->Unit = {}) : UpdateStub<Data>(updateParent) {

    private class StopException: Exception()

    //private val updateStack by DynamicArrayList<Update<Data>>(arrayListOf())
    /*
    private val updateActor = CoroutineScope(Job()).broadcast<Update<Data>>(capacity = 10_000) {
        for(update in channel) {
            updateStack.add(update)
        }
    }

     */

    private val inputStack: DynamicArrayList<I> = DynamicArrayList<I>(arrayListOf())
    /*
    private val inputActor = CoroutineScope(Job()).actor<I>(capacity = 10_000) {
        for(input in channel) {
            inputStack.add(input)
        }
    }

     */

    private val updateStackIsEmpty = updateStack.isEmpty
    private val inputStackIsEmpty = inputStack.isEmpty
    private val stacksAreNonEmpty = !(updateStackIsEmpty and inputStackIsEmpty)

    override val flow by lazy{ by(
        SimpleProcessFlow(
            onStart,
            {data ->
                blockUntil(stacksAreNonEmpty){it}
                if(!(updateStackIsEmpty.value)){
                    with(by(updateStack.pop())){
                        try {
                            if(this(data).data != data){
                                Phase.Wait(onUpdate(this(data)))
                            } else {
                                Phase.Wait(data)
                            }
                        } catch(exception: StopException) {
                            Phase.Stop(data)
                        }
                    }
                } else {
                    onInput(inputStack.pop(),data)
                }
            },
            onStop
        )
    )}

    override val morphism: suspend CoroutineScope.(Data) -> Evolving<Data> = {
            data -> flow(Phase.Start(data)) map { it.data }
    }

    abstract val onInput: suspend CoroutineScope.(I,Data)->Phase<Data>

    @EvoleqDsl
    suspend fun input(input: I) {
        inputStack.add(input)
        //inputActor.send(input)
    }

    @EvoleqDsl
    override  fun closePorts() {
       // updateActor.close()
      //  inputActor.close()
    }
}