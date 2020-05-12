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


interface ScopedSuspendedState<S, T> : ScopedSuspended<S, Pair<T, S>> {
    suspend fun <T1> map(f: suspend CoroutineScope.(T) -> T1): ScopedSuspendedState<S, T1> = ScopedSuspendedState {
            s -> with(by(this@ScopedSuspendedState)(s)){
        Pair(f(first),second)
    }
    }
}
@Suppress("FunctionName")
@EvoleqDsl
fun <S, T> ScopedSuspendedState(state: suspend CoroutineScope.(S)->Pair<T, S>): ScopedSuspendedState< S, T> = object : ScopedSuspendedState<S, T> {
    override val function: suspend CoroutineScope.(S) -> Pair<T, S>
        get() = state

}



fun <S, T> ScopedSuspendedState<S, ScopedSuspendedState<S, T>>.multiply() : ScopedSuspendedState<S, T> = ScopedSuspendedState {
        s -> with(by(this@multiply)(s)){
    by(first)(second)
}
}

interface KlScopedSuspendedState<B, S, T> : ScopedSuspended<S, ScopedSuspendedState<B, T>>

@Suppress("FunctionName")
fun<B, S, T>  KlScopedSuspendedState(arrow: suspend CoroutineScope.(S)->ScopedSuspendedState<B, T>): KlScopedSuspendedState<B, S, T> = object : KlScopedSuspendedState<B, S, T> {
    override val function: suspend CoroutineScope.(S) -> ScopedSuspendedState<B, T>
        get() = arrow
}

suspend operator fun <B, R, S, T> KlScopedSuspendedState<B, R, S>.times(other: KlScopedSuspendedState<B, S, T>): KlScopedSuspendedState<B, R, T> =
    KlScopedSuspendedState {
            r -> by(this@times)(r).map(by(other)).multiply()
    }