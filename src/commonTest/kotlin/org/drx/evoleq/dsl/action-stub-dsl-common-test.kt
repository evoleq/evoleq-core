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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.drx.dynamics.Dynamic
import org.drx.evoleq.evolution.stubs.ActionStub
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.parallel
import org.drx.evoleq.test.runTest
import org.evoleq.math.cat.suspend.morphism.by
import org.evoleq.math.cat.suspend.morphism.evolve
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase

class ActionStubDslTestCommon {

    @ExperimentalCoroutinesApi
    @Test
    fun shouldCallOnUpdateWhenSelfUpdateIsCalled () = runTest {
        val startValue = 0
        val addValue = 2
        val resultValue = startValue + addValue
        val updateCalled by Dynamic(startValue)
        val stub = actionStub<String, Int> {
            id(ActionStub::class)
            onInput { input, x ->
                when(input) {
                    "update" -> Phase.Wait(selfUpdate(x){ y -> y+addValue})
                    else -> Phase.Stop(x)
                }
            }
            onUpdate{
                updated -> with(updated){
                    updateCalled.value = data
                    data
                }
            }
        }
        var result: Evolving<Int>? = null
        parallel {
            result = evolve(startValue) by stub
        }
        stub.input("update")
        stub.input("stop")
        delay(2_000)
        assertEquals(result!!.get(), resultValue)
        assertEquals(updateCalled.value, resultValue)
    }

    //@Test
    @ExperimentalCoroutinesApi
    fun handleHeavyLoadOfInputs() = runTest {
        var c = 0
        val stopped by Dynamic(false)
        val stub = actionStub<String,Int>{
            id(ActionStub::class)
            onInput { input, data ->
                when(input) {
                    "update" -> {
                        println(data)
                        //c = data +1
                        Phase.Wait(data + 1)
                    }
                    "stop" -> Phase.Stop(data)
                    else -> Phase.Wait(data)
                }
            }
            onStop{
                data ->
                stopped.value = true
                data
            }
        }
        val result by Dynamic<Evolving<Int>?>(null)
        CoroutineScope(Job()).parallel{
            result.value = evolve(0) by stub
        }
        //delay(1500)
        CoroutineScope(Job()).parallel{
            //delay(2_000)
            (1..1_000).forEach { _ ->
                //delay(10)
                val rand = Random.nextLong(from = 5, until= 10)
                println("randon-delay = $rand")
                delay(rand)
                parallel {
                    stub.input("update")
                }

            }
            delay(1_000)
            stub.input("stop")
        }



        while (result.value == null) {
            //println("delaying ...")
            delay(10)
        }

        //println(result.value!!.get())

    }

}