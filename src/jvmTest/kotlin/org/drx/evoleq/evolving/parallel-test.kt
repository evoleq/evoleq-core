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
import org.drx.dynamics.Dynamic
import org.junit.Test
import kotlin.system.measureTimeMillis

class ParallelTest {

    @Test fun `execution should start on initialization of Parallel`() = runBlocking {
        val parallel by Parallel{delay(1_000)}
        delay(1_000)
        assert(
            measureTimeMillis { parallel.get() } < 1_000
        )
    }

    @Test fun `execution should happen in parallel`() = runBlocking{
        val result by Dynamic<Pair<Int,Int>?>(null)
        val time = measureTimeMillis {
            val parallel1 by Parallel {
                delay(1_000)
                1
            }
            val parallel2 by Parallel {
                delay(1_000)
                2
            }
            result.value = Pair(parallel1.get(), parallel2.get())
        }
        result.subscribe(this::class){
            it!!
            assert(time >= 1_000)
            assert(time <= 1_500)
            assert( it.first == 1 )
            assert( it.second == 2 )
        }
    }

    @Test fun `a mapped Parallel should take the right value`() = runBlocking {
        val f: suspend CoroutineScope.(Int)->String = { x -> "${x +1}"}
        val mapped by Parallel{ 0} map f
        assert(mapped.get() == "1")
    }

    @Test fun `a mapped Parallel should execute on initialization`() = runBlocking {
        val f: suspend CoroutineScope.(Int)->String = { x -> "${x +1}"}
        val mapped by Parallel{delay(1_000); 0} map { x -> "${x +1}"}
        delay(1_000)
        assert(
            measureTimeMillis { mapped.get() } < 1_000
        )
    }

    @Test fun `fish operator`() {

    }
}