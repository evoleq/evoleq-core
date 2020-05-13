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
import kotlin.reflect.KProperty

interface KlParallel<S, Data> : KlEvolving<S, Data> {
    override val morphism: suspend CoroutineScope.(S) -> Parallel<Data>
    override fun getValue(thisRef: Any?, property: KProperty<*>): suspend CoroutineScope.(S) ->Parallel<Data> = morphism
}

@Suppress("FunctionName")
fun <S, Data> KlParallel(arrow: suspend CoroutineScope.(S)->Parallel<Data>): KlParallel<S, Data> = object : KlParallel<S, Data> {
    override val morphism: suspend CoroutineScope.(S) -> Parallel<Data> = arrow
}

suspend operator fun <R, S, T> KlParallel<R, S>.times(other: KlParallel<S, T>): KlParallel<R, T> = KlParallel (
    morphism * other.morphism
)