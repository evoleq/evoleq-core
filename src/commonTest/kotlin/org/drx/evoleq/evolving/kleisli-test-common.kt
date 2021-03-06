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
package org.drx.evoleq.evolving

import kotlinx.coroutines.delay
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.flow
import org.drx.evoleq.dsl.stub
import org.drx.evoleq.evolution.Flow
import org.drx.evoleq.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Check that various kleisli-evolvings behave correctly when mapped as their super types
 */
class KleisliTestCommon {

    @ExperimentalTime
    @Test
    fun kleisliProductOfCastedKleisliParallelsBehavesLikeAKleisliParallel() = runTest{
        // Let
        fun klEv(name: String) = KlParallel{ x: Int -> parallel{delay(500);println(name);x} } as KlEvolving<Int,Int>
        // We have
        val p = klEv("p")
        with((p*p).morphism(DefaultEvolvingScope(),0)){
            // first let's see that p+p(0) has the right type
            assertTrue(this is Parallel<Int>)
            // second, let's see that computation has already been done
            assertTrue(measureTime {
                    assertTrue(get() == 0)
                }.inMilliseconds
             < 500)
        }

        // But, on the other hand,  also
        val q = klEv("q")
        assertTrue(measureTime{(q*q).morphism(DefaultEvolvingScope(),0).get()}.inMilliseconds >= 1_000)
        // which shows that p*p(0) is initialized immediately., like expected.
        // Moreover, we have
        with(DefaultEvolvingScope()) {
            val p1 by p * p
            assertTrue(p1(0) is Parallel<Int>)

            val f by p
            assertTrue((f * f)(0) is Parallel<Int>)
        }
    }

    @Test fun treatFlowAsKleisli() {
        val flow1 by stub<Int>{
            id(Flow::class)
            evolve{
                x -> parallel{ x +1 }
            }
        }.flow<Int, Boolean>(conditions{
            testObject(true)
            check{value -> value}
            updateCondition { x -> x < 10 }
        })
        
    }
    
 
}