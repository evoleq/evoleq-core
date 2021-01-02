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
import org.drx.evoleq.evolution.stubs.UpdateStub
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.evolving.parallel
import org.evoleq.math.cat.suspend.morphism.by
import org.evoleq.math.cat.suspend.morphism.evolve
import org.evoleq.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateStubDslCommonTest {

    @ExperimentalCoroutinesApi
    @Test
    fun checkBasicFunctionality() = runTest{
        class Id
        class Outer
        val dynResult by Dynamic(-1)
        val started by Dynamic(false)
        val updatePerformed by Dynamic(false)
        val stopped by Dynamic(false)
        val updateStub = updateStub<Int> stub@{
            id(Id::class)
            onStart{
                data -> started.value = true;data+1
            }
            onUpdate { updated -> with(updated.data) {
                updatePerformed.value = true
                this
            } }

            onStop{
                data -> stopped.value = true;data - 1
            }
        }

        parallel{
            val result = updateStub.morphism(this,0).get()
            assertTrue(result == 1)
            dynResult.value = result
        }
        updateStub.update(Outer::class){data -> data +1}
        updateStub.stop()
        delay(100)

        assertTrue(dynResult.value == 1)
        assertTrue(started.value)
        assertTrue(updatePerformed.value)
        assertTrue(stopped.value)
    }

    @ExperimentalCoroutinesApi
    //@Test
    fun updateChildStub() = runTest {
        class Parent
        class Child
        class Outer
        data class Data(val x: Int, val s: String)
        suspend fun Data.x(set: suspend Int.()->Int): Data = Data(x.set(),s)//copy(x = x.set())

        var updateResult: Int = 0

        val updateStub = updateStub<Data> {
            id(Parent::class)
            onStart { data ->
                println("Started parent")
                //parallel {
                    process<Int>(child(Child::class)) on data.x
                    println("Returning from onStart of parent")
                //}
                data
            }
            onUpdate { updated ->
                println("updateStub.onUpdate...")
                if (updated.senderId != Child::class) {
                    updateChild<Int>(childId = Child::class) { _->
                        println("Updating")
                        updated.data.x
                    }
                }
                updated.data
            }
            onStop { data ->
                data
            }
            updatableChild<Int>(Child::class) {
                onStart { data ->
                    println("Started child")
                    data
                }
                onUpdate { updated ->
                    println("child.onUpdate: senderId = ${updated.senderId.simpleName}")
                    Parallel {
                    updateResult = 1
                    }
                    updated.data
                }
                onStop { data -> data }
            }

                //parentStub = stub()

        }
        Parallel{
            val result = evolve(Data(0,"")) by updateStub
            assertEquals(result.get(), Data(1,""))
            println("result checked")
        }

        updateStub.update(senderId = Outer::class) { data ->/*println("updating child");*/data.x { 1 } }

        delay(1_000)
        println(updateResult)
        assertTrue(updateResult == 1)
        updateStub.stop()
        //}
    }

    @ExperimentalCoroutinesApi
    @Test fun updateParentFromChildStub() = runTest {
        // Ids
        class Outer
        class Parent
        class Child
        // Data class to work with
        data class Data(val x: Int)
        suspend fun Data.x(set: suspend Int.()->Int) = copy(x = x.set())
        // Value is to be set on update of parent stub
        val updatedData by Dynamic(Data(0))
        // When set, use it to perform an update. Such updates should lead to an update ot parent
        var child: UpdateStub<Int>? = null

        val parent = updateStub<Data>{
            id(Parent::class)
            onStart { data ->
                process<Int>(child(Child::class)) on data.x
                data
            }
            onUpdate { updated ->
                with(updated) {
                    println("parent.onUpdate: senderId = ${senderId.simpleName}")
                    if (senderId != Child::class){
                        updateChild<Int>(Child::class){
                            data.x
                        }
                    }
                    updatedData.value = data
                    data
                }
            }
            onStop{ data ->
                (child<Int>(Child::class) as UpdateStub<Int>).stop()
                data
            }
            updatableChild<Int>(Child::class){
                onStart{ data ->
                    data
                }
                updateParentBy<Data> {
                        setter -> { data: Data -> data.x{ setter(this) } }
                }
                onUpdate { updated -> with(updated){
                    println("child.onUpdate: senderId = ${senderId.simpleName}")
                    if(senderId != Parent::class){
                        updateParent<Data> { _->data }
                    }
                    data
                } }
                onStop{data ->
                    data
                }
                Parallel{
                    child = stub()
                }
            }
        }
        delay(1_000)
        Parallel {
            val res = by(parent)(Data(0))
            assertEquals(res.get(), Data(1))
        }
        child!!.update(Outer::class) { _ -> 1 }
        delay(100)
        assertEquals(updatedData.value, Data(1))
        parent.stop()
        delay(500)
    }

    @Test fun updateStubAsChildOfStandardStubShouldIntegrateProperly() = runTest {
        data class Data(val x: Int)

        class Parent
        class Child
        val stub = stub<Data> {
            id(Parent::class)
            evolve{
                data -> parallel{data}
            }
            updatableChild<String>(Child::class){
                // no further configuration is necessary
            }
        }
        assertTrue(stub.stubs.size == 1)
        assertTrue(stub.stubs[Child::class] is UpdateStub)
        assertEquals(stub.stubs[Child::class]!!.parent, stub)
    }
}