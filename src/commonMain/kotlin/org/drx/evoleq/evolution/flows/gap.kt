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
package org.drx.evoleq.evolution.flows

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolution.Flow
import org.drx.evoleq.evolution.Gap
import org.drx.evoleq.evolution.fill

suspend fun <P, W, T> Flow<P, T>.close (gap: Gap<W, P>,conditions: EvolutionConditions<W,T>): Flow<W,T> = Flow(
    conditions,
    gap.fill(this)
)

suspend infix fun <P, W, T> Flow<P, T>.close (gap: Gap<W, P>):suspend (EvolutionConditions<W,T>)->Flow<W,T> = {
    conditions -> this@close.close(gap,conditions)
}

suspend infix fun <W, T> (suspend (EvolutionConditions<W,T>)->Flow<W,T>).under(conditions: EvolutionConditions<W, T>): Flow<W, T> = this(conditions)