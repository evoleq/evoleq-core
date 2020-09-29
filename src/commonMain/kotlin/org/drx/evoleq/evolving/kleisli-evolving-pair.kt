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
import org.drx.evoleq.dsl.EvoleqDsl


interface KlEvolvingPair<S, F1, F2> : KlEvolving<S, Pair<F1,F2>>

@EvoleqDsl
@Suppress("FunctionName")
fun <S, F1, F2>KlEvolvingPair(arrow: suspend CoroutineScope.(S)->Evolving<Pair<F1,F2>>): KlEvolvingPair<S, F1, F2> = object : KlEvolvingPair<S, F1, F2> {
    override val morphism: suspend CoroutineScope.(S) -> Evolving<Pair<F1,F2>> = arrow
}