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

import kotlinx.coroutines.*
import org.drx.dynamics.Dynamic
import org.drx.dynamics.ID
import org.drx.dynamics.exec.blockUntil
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.type.Maybe
import org.drx.evoleq.type.OnSet
import org.drx.evoleq.type.map
import kotlin.reflect.KProperty

open class Parallel<out Data>(override  val scope: CoroutineScope = DefaultEvolvingScope(), open val  block: suspend CoroutineScope.()->Data) : Evolving<Data> {
    private val maybe by Dynamic<Maybe<Data>>(Maybe.Nothing())
    init {
        scope.launch { maybe.value  = withContext(Dispatchers.Default) {
            coroutineScope {
                Maybe.Just(block())
            }
        } }
    }

    @EvoleqDsl
    override suspend fun get(): Data {
        blockUntil(maybe){ value -> value is Maybe.Just }
        return (maybe.value as Maybe.Just).value
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Parallel<Data> = this

    @EvoleqDsl
    @Suppress("unchecked_cast")
    override suspend infix fun < T> map(f: suspend CoroutineScope.(Data)->T): Parallel<T>  = with(CoroutineScope(SupervisorJob())) scope@{
        this+scope.coroutineContext
        parallel {
            with(onset { s -> async{ this@scope.f(s) } }) maybe@{
                blockUntil(this){ maybe -> maybe is Maybe.Just }
                if(this@scope.coroutineContext[Job]!!.isCancelled){
                    throw Exception("computation has been cancelled")
                } else {
                    (this@maybe.value as Maybe.Just).value.await()
                }
            }
        }
    }

    internal suspend fun <T> onset(id: ID = OnSet::class, f:  (Data)->T) = maybe.push(id){it map f}
}

@EvoleqDsl
fun <Data> CoroutineScope.parallel(block: suspend CoroutineScope.() -> Data): Parallel<Data> = Parallel(this, block)

@EvoleqDsl
fun <Data> CoroutineScope.parallel(data: Data): Parallel<Data> = parallel{data}

@EvoleqDsl
@Suppress("unchecked_cast")
suspend fun <Data> Parallel<Parallel<Data>>.multiply(): Parallel<Data> = (onset { p -> p }.value as Maybe.Just<*>).value as Parallel<Data>

suspend operator fun <R, S, T> (suspend CoroutineScope.(R)->Parallel<S>).times(other: suspend CoroutineScope.(S)->Parallel<T>): suspend CoroutineScope.(R)->Parallel<T> = {
        r -> (this@times(r) map other).multiply()
}



