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

import org.drx.dynamics.ID
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.dsl.StubConfiguration
import org.drx.evoleq.dsl.configuration

interface Stub<Data> : Evolver<Data> {
    val id: ID
    val parent: Stub<*>?
    val stubs: HashMap<ID, Stub<*>>
}

@EvoleqDsl
operator fun <D> Stub<D>.get(childId: ID): Stub<*>? = stubs[childId]

@EvoleqDsl
operator fun <D>Stub<D>.set(childId: ID, child: Stub<*>) {
    stubs[id] = with(child.configuration() as Pair<out StubConfiguration<*>, StubConfiguration<*>.()->Unit>) {
        val conf = first
        val confMap = second
        conf.parent = this@set
        conf.confMap()
        conf.configure()
    }
}

@EvoleqDsl
@Suppress("unchecked_cast")
fun <E> Stub<*>.find(id: ID): Stub<E>? = (stubs[id] as Stub<E>?)?:with(stubs.values) {
    forEach {
        val res = it.find<E>(id)
        if (res != null) {
            return@with res
        }
    }
    return@with null
}





