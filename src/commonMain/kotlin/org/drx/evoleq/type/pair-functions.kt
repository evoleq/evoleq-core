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
package org.drx.evoleq.type

import kotlinx.coroutines.CoroutineScope
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.EvolvingPair
import org.drx.evoleq.evolving.KlEvolving
import org.drx.evoleq.evolving.evolving

infix fun <S1, S2, T1, T2> KlEvolving<S1,T1>.pair(other: KlEvolving<S2,T2>): KlEvolving<Pair<S1,S2>,Pair<T1,T2>> = KlEvolving scope@{ pair ->
    with( Pair(
            this@pair.morphism(this, pair.first),
            other.morphism(this, pair.second))
    ) {
        object : EvolvingPair<T1, T2> {
            override val scope: CoroutineScope = this@scope
            override val f1: Evolving<T1> = first
            override val f2: Evolving<T2> = second
        }
    }
}

fun <S,T, W> KlEvolving<S, T>.pairWithId():  KlEvolving<Pair<S,W>,Pair<T,W>> = this pair KlEvolving { w: W -> evolving{w}}