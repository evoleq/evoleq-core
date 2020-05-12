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
import kotlinx.coroutines.plus

interface EvolvingPair<F1, F2> : Evolving<Pair<F1, F2>> {
    val f1: Evolving<F1>
    val f2: Evolving<F2>
    override suspend fun get(): Pair<F1, F2> = Pair(f1.get(), f2.get())
}

@Suppress("FunctionName")
fun <F1, F2> EvolvingPair( f1: Evolving<F1>, f2: Evolving<F2>) : EvolvingPair<F1, F2> = object : EvolvingPair<F1, F2> {
    val myScope = DefaultEvolvingScope()
    override val scope: CoroutineScope
        get() = myScope + f1.scope.coroutineContext + f2.scope.coroutineContext
    override val f1: Evolving<F1>
        get() = f1
    override val f2: Evolving<F2>
        get() = f2
}
