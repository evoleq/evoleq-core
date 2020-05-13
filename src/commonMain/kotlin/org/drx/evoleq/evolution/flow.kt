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

import kotlinx.coroutines.CoroutineScope
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.KlEvolving

open class Flow<D, T>(
    internal val conditions: EvolutionConditions<D, T>,
    private val phi: KlEvolving<D, D>
) : Evolver<D> {
    override val morphism: suspend CoroutineScope.(D) -> Evolving<D> = { data: D ->
        org.drx.evoleq.evolve(
            data,
            conditions,
            this,
            phi.morphism
        )
    }
}



