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
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.system.measureTimeMillis

class OnDemandTest {
    @Test fun  `computation should happen on demand`() = runBlocking {
        val onDemand by OnDemand{delay(1_000); 1}
        val time = measureTimeMillis {
            delay(1_000)
            assert(measureTimeMillis {
                onDemand.get()
            } >= 1_000)
        }
        assert(time >=2_000)
    }

    @Test fun `value should be computed only once`() = runBlocking {
        val onDemand by OnDemand{delay(1_000); 1}
        onDemand.get()
        assert(measureTimeMillis { assert(onDemand.get() == 1) } < 1_000)
    }

    @Test fun `a mapped OnDemand should compute the right value`() = runBlocking{
        val f : suspend CoroutineScope.(Int)->String = {x -> "${x+1}"}
        val mapped by OnDemand{0} map f
        assert(mapped.get() == "1")
    }

    @Test fun `a mapped OnDemand should be executed on demand`() = runBlocking{
        val f: suspend CoroutineScope.(Int)->String = { x -> "${x +1}"}
        val mapped by OnDemand{delay(1_000); 1} map f
        delay(1_000)
        assert(
            measureTimeMillis { mapped.get() } >= 1_000
        )
    }
}