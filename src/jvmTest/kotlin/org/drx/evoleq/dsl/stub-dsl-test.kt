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
import org.drx.evoleq.evolution.Stub
import org.drx.evoleq.evolution.find
import org.drx.evoleq.evolving.evolving
import org.drx.evoleq.evolving.parallel
import org.drx.evoleq.type.ParentId
import org.junit.Test

class StubDslTest {
    @Test fun `parent child relation`() = runBlocking {
        class Child0
        class Child1
        val childStub = stub<Int> {
            id(Child0::class)
            evolve{x -> evolving{x}}
        }
        val parent = stub<Int> {
            id(ParentId::class)
            evolve { x: Int -> evolving(x+1)}
            child(childStub)
            child(Child1::class,childStub)
            child<Int>(Stub::class){
                evolve{x -> evolving {
                    x*x
                }}
            }
        }
        assert(parent.stubs.size == 3)

        val child = parent.stubs[Stub::class]!! as Stub<Int>
        child.parent!!
        assert(child.parent!!.id == ParentId::class)
        val child0 = parent.stubs[Child0::class]!! as Stub<Int>
        child0.parent!!
        assert(child0.parent!!.id == ParentId::class)
    }

    @Test fun `should find child stubs`() {
        class Class0
        class Class1
        val stub = stub<Unit>{
            id(Stub::class)
            evolve{_-> parallel{Unit}}
            child<Unit>(Class0::class){
                evolve{_-> parallel{Unit}}
                child<Unit>(Class1::class){
                    evolve{_-> parallel{Unit}}
                }
            }
        }
        val child0 = stub.find<Unit>(Class0::class)
        child0!!
        val child1 = stub.find<Unit>(Class1::class)
        child1!!

    }

    @Test fun `evolution by delegation`() = runBlocking{
        val evolve by stub<Int>{
            id(Stub::class)
            evolve{
                x -> parallel { x+1 }
            }
        }
        delay(100)
        assert(evolve(1).get() == 2)

    }
}