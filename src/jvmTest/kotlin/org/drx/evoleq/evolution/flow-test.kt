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
package org.drx.evoleq.evolution

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.evolving.DefaultEvolvingScope
import org.drx.evoleq.evolving.KlParallel
import org.drx.evoleq.evolving.parallel
import org.junit.Test

class FlowTest {
    @Test fun `basic functionality should work`()  = runBlocking {
        val result by Flow<Int,Boolean>(
            conditions{
                testObject(true )
                check{ b -> b }
                updateCondition { x -> x < 10 }
            },
            KlParallel { x -> parallel { x + 1 } }
        )
        delay(1_000)
        with(DefaultEvolvingScope()) {
            assert(result(0).get() == 10)
        }
    }

}