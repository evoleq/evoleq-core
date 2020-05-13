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
import org.drx.evoleq.dsl.EvoleqDsl
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


interface ScopedSuspended<in S, out T> : ReadOnlyProperty<Any?, suspend CoroutineScope.(S) -> T> {
    val morphism: suspend CoroutineScope.(S)->T

    override fun getValue(thisRef: Any?, property: KProperty<*>): suspend CoroutineScope.(S) -> T = { s ->morphism(s)}

    fun onScope(scope: CoroutineScope): Suspended<S,T> = Suspended{ s: S -> morphism(scope,s)}

    open suspend operator fun<T1> times(other: ScopedSuspended<T,T1>): ScopedSuspended<S, T1> = ScopedSuspended {
            s -> other.morphism( this, morphism(s) ) }
}

@Suppress("FunctionName")
@EvoleqDsl
fun <S, T> ScopedSuspended(function: suspend CoroutineScope.(S)->T): ScopedSuspended<S, T> = object : ScopedSuspended<S, T> {
    override val morphism: suspend CoroutineScope.(S) -> T = function
}

/**
 * Force delegation by function
 */
@EvoleqDsl
fun <S, T> by(arrow: ScopedSuspended<S, T>): suspend CoroutineScope.(S)->T = arrow.morphism

@EvoleqDsl
fun <S> CoroutineScope.evolve(data: S): Pair<CoroutineScope,S> = Pair(this,data)

@EvoleqDsl
suspend infix fun <S, T> Pair<CoroutineScope,S>.by(arrow: ScopedSuspended<S, T>): T = arrow.morphism(first,second)
