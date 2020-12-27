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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import org.evoleq.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class OnDemandTestCommon {
    @ExperimentalTime
    @Test
    fun  computationShouldHappenOnDemand() = runTest {
        val onDemand by OnDemand{delay(1_000); 1}
        val time = measureTime {
            delay(1_000)
            assertTrue(measureTime {
                onDemand.get()
            }.inMilliseconds >= 1_000)
        }.inMilliseconds
        assertTrue (time >=2_000)
    }

    @ExperimentalTime
    @Test fun valueShouldBeComputedOnlyOnce() = runTest {
        val onDemand by OnDemand{delay(1_000); 1}
        onDemand.get()
        assertTrue(measureTime { assertTrue(onDemand.get() == 1) }.inMilliseconds < 1_000)
    }

    @Test fun aMappedOnDemandShouldComputeTheRightValue() = runTest{
        val f : suspend CoroutineScope.(Int)->String = {x -> "${x+1}"}
        val mapped by OnDemand{0} map f
        assertTrue(mapped.get() == "1")
    }

    @ExperimentalTime
    @Test fun aMappedOnDemandShouldBeExecutedOnDemand() = runTest{
        val f: suspend CoroutineScope.(Int)->String = { x -> "${x +1}"}
        val mapped by OnDemand{delay(1_000); 1} map f
        delay(1_000)
        assertTrue(
            measureTime { mapped.get() }.inMilliseconds >= 1_000
        )
    }
}