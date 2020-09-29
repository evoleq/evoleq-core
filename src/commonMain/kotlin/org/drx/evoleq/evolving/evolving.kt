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
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import org.drx.evoleq.dsl.EvoleqDsl
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("FunctionName")
fun DefaultEvolvingScope() = CoroutineScope(Job())

interface Evolving<out Data> : ReadOnlyProperty<Any?,Evolving<Data>> {
    val scope: CoroutineScope
    suspend fun get(): Data
    override fun getValue(thisRef: Any?, property: KProperty<*>): Evolving<Data> = this
    suspend infix fun <T> map(f: suspend CoroutineScope.(Data)->T): Evolving<T> = coroutineScope { evolving(f(get())) }
}

@EvoleqDsl
fun <Data> Evolving<Data>.job() = scope.coroutineContext[Job]

@EvoleqDsl
fun <Data> CoroutineScope.evolving(data: Data): Evolving<Data> =
    object : Evolving<Data> {
        override val scope: CoroutineScope
            get() = this@evolving

        override suspend fun get(): Data = data
    }



@EvoleqDsl
fun <Data> CoroutineScope.evolving(block: suspend CoroutineScope.() -> Data): Evolving<Data> =
    object : Evolving<Data> {
        override val scope: CoroutineScope
            get() = this@evolving
        override suspend fun get(): Data = coroutineScope { block() }
    }

@EvoleqDsl
suspend fun <Data> Evolving<Evolving<Data>>.multiply(): Evolving<Data> = get()

suspend operator fun <R, S, T> (suspend CoroutineScope.(R)->Evolving<S>).times(other: suspend CoroutineScope.(S)->Evolving<T>): suspend CoroutineScope.(R)->Evolving<T> = {
    r -> (this@times(r) map other).multiply()
}