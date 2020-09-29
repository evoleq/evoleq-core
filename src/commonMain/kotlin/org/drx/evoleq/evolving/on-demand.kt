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
import org.drx.dynamics.exec.blockUntil
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.type.Maybe
import kotlin.reflect.KProperty

open class OnDemand<out Data>(override val scope: CoroutineScope = DefaultEvolvingScope(), val block: suspend CoroutineScope.()->Data) : Evolving<Data> {
    private val maybe by Dynamic<Maybe<Data>>(Maybe.Nothing())

    @EvoleqDsl
    override suspend fun get(): Data {
        if(maybe.value is Maybe.Nothing<*>) {
            scope.launch {
                maybe.value = withContext(Dispatchers.Default) {
                    coroutineScope {
                        Maybe.Just(block())
                    }
                }
            }
            blockUntil(maybe) { value -> value is Maybe.Just<*> }
        }
        return (maybe.value as Maybe.Just).value
    }

    @EvoleqDsl
    override suspend infix fun <T> map(f: suspend CoroutineScope.(Data)->T): OnDemand<T> = with(CoroutineScope(SupervisorJob())) scope@{
        this+scope.coroutineContext
        onDemand {
            f(get())
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): OnDemand<Data> = this

    //internal fun <T> onset(id: ID = OnSet::class, f: (Data)->T) = maybe.push(id){it map f}
}

@EvoleqDsl
fun <Data> CoroutineScope.onDemand(block: suspend CoroutineScope.() -> Data): OnDemand<Data> = OnDemand(this, block)