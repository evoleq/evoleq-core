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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import org.drx.dynamics.Dynamic
import org.drx.evoleq.evolution.get
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase.Stop
import org.drx.evoleq.evolution.stubs.InputStub
import org.drx.evoleq.evolving.parallel
import org.evoleq.math.cat.suspend.morphism.by
import org.evoleq.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class InputStubDslCommonTest {

    @ExperimentalCoroutinesApi
    @Test
    fun checkCasicFunctionality () = runTest {
        data class Data(val state: String)
        val state by Dynamic(Data(""))
        var onStartCalled = false
        var onStopCalled = false
        val inputStub = inputStub<String, Data> {
            id(InputStub::class)
            onStart{data ->
                onStartCalled = true
                data
            }
            onInput { s, data ->
                when(s) {
                    "state_1" -> {
                        state.value = Data("state_2")
                        org.drx.evoleq.evolution.phase.process.SimpleProcessPhase.Wait(Data("state_2"))
                    }
                    "state_2" -> Stop(data)
                    else -> Stop(data)
                }
            }
            onStop { data ->
                onStopCalled = true
                data
            }
        }
        parallel {
            val res = by(inputStub)(Data("start"))
        }
        inputStub.input("state_1")
        delay(100)
        assertTrue(state.value.state == "state_2")
        inputStub.input("state_2")
        delay(100)
        assertTrue(onStartCalled)
        delay(100)
        assertTrue(onStopCalled)
    }

    @Test fun parentChildRelations () = runTest {
        class Parent
        class Child1
        class Child2

        val child2 = inputStub<Boolean, Boolean> {
            id(Child2::class)
            onInput{x,y->Stop(y && x)}
        }

        val parent = inputStub<String, Int> {
            id(Parent::class)
            onInput { i, data -> Stop(data) }

            inputChild<Int,Boolean>(Child1::class){
                onInput{j, data -> Stop(data)}
            }
            inputChild(child2)
        }

        assertTrue(parent.stubs.size == 2)
        val c1 = parent[Child1::class]!!
        val c2 = parent[Child2::class]!!
        assertTrue(c1 is InputStub<*,*>)
        assertTrue(c1.parent == parent)
        assertTrue(c2 is InputStub<*,*>)
        assertTrue(c2.parent == parent)


    }
}