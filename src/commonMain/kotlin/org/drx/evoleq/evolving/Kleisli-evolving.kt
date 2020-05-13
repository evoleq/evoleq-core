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
import org.drx.evoleq.type.ScopedSuspended
import kotlin.reflect.KProperty

interface KlEvolving<S,Data> : ScopedSuspended<S, Evolving<Data>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): suspend CoroutineScope.(S) -> Evolving<Data> = morphism

    suspend infix fun <T> map(f: suspend CoroutineScope.(Data)->T) = KlEvolving<S, T> {
        s -> morphism(s) map f
    }
}

@Suppress("FunctionName")
fun <S, Data> KlEvolving(arrow: suspend CoroutineScope.(S)->Evolving<Data>): KlEvolving<S, Data> = object : KlEvolving<S, Data> {
    override val morphism: suspend CoroutineScope.(S) -> Evolving<Data> = arrow


}

suspend operator fun <R, S, T> KlEvolving<R, S>.times(other: KlEvolving<S, T>): KlEvolving<R, T> = KlEvolving (
    morphism * other.morphism
)


