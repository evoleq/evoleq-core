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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.dynamics.Dynamic
import org.drx.evoleq.evolution.stubs.ActionStub
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.parallel
import org.evoleq.math.cat.suspend.morhism.by
import org.evoleq.math.cat.suspend.morhism.evolve
import org.junit.Test
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase as Phase

class ActionStubDslTest {

    @Test fun `should call onUpdate when selfUpdate is called` () = runBlocking {
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
        delay(1_000)
        assert(result!!.get() == resultValue)
        assert(updateCalled.value == resultValue)
    }

}