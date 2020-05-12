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

sealed class Maybe<out T> {
    data class Just<T>(val value: T) : Maybe<T>()
    class Nothing<T> : Maybe<T>()
}

infix fun <S, T> Maybe<S>.map(f: (S)->T): Maybe<T> = when(this) {
    is Maybe.Just -> try{
        Maybe.Just(f(value))
    } catch(exception: Exception){
        Maybe.Nothing<T>()
    }
    is Maybe.Nothing -> Maybe.Nothing<T>()
}

suspend infix fun <S, T> Maybe<S>.mapSuspended(f: suspend (S)->T): Maybe<T> = when(this) {
    is Maybe.Just -> try{
        Maybe.Just(f(value))
    } catch(exception: Exception){
        Maybe.Nothing<T>()
    }
    is Maybe.Nothing -> Maybe.Nothing<T>()
}