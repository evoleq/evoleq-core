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

import org.drx.evoleq.evolving.*
import org.evoleq.math.cat.suspend.morhism.by

interface Gap<W, P> {
    val from: KlEvolving<W, Pair<W, P>>
    val to: KlEvolving<Pair<W, P>, W>
}

suspend fun <W, P> Gap<W, P>.fill(filler: KlEvolvingPair<Pair<W, P>, W, P>): KlEvolving<W, W> =
    from * filler * to

suspend fun <W, P> Gap<W, P>.fill(filler: KlEvolving<P, P>): KlEvolving<W, W> =
    with(KlEvolvingPair { pair: Pair<W, P> ->
        EvolvingPair(
            evolving { pair.first },
            by(filler)(pair.second)
        )
    }) {
        from * this * to
    }

/*
suspend operator fun <W, P, Q> Gap<W, P>.times(other: Gap<P, Q>): ScopedSuspended<P,Gap<W, Q>> {
    val preFrom = this@times.from * KlEvolving { pair: Pair<W, P> -> EvolvingPair(evolving{pair.first}, other.from.function(this,pair.second)) }
    val preTo = KlEvolving {pair: Pair<W, Pair<P, Q>> -> EvolvingPair(evolving{pair.first}, other.to.function(this,pair.second))  } * to
    val fromMap ={p:P -> KlEvolving { w:W -> preFrom.function(this,w) map {pair: Pair<W, Pair<P, Q>> -> Pair(pair.first, pair.second.second) } }}
    val toMap =  {p:P -> KlEvolving { pair: Pair<W, Q> -> preTo.function(this, Pair(pair.first, Pair(p,pair.second))) } }
    return ScopedSuspended { p:P ->
        gap{
            from(fromMap(p))
            to(toMap(p))
        }
    }
}

 */