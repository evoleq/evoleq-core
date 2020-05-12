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
package org.drx.evoleq.evolution.stubs

import kotlinx.coroutines.CoroutineScope
import org.drx.dynamics.ID
import org.drx.evoleq.evolution.Stub
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.parallel

open class SimpleProcessStub<Data>(
    val onStart: suspend CoroutineScope.(Data)->Data,
    val onWaiting: suspend CoroutineScope.(Data)->SimpleProcessPhase<Data>,
    val onStop: suspend CoroutineScope.(Data)->Data
) : Stub<SimpleProcessPhase<Data>>  {

    override val id: ID
        get() = SimpleProcessStub::class

    override val parent: Stub<*>?
        get() = null

    override val stubs: HashMap<ID, Stub<*>>
        get() = HashMap()

    override val function: suspend CoroutineScope.(SimpleProcessPhase<Data>) -> Evolving<SimpleProcessPhase<Data>>
        get() = {
            phase -> when(phase) {
                is SimpleProcessPhase.Start -> parallel {
                    SimpleProcessPhase.Wait( onStart(phase.data) )
                }
                is SimpleProcessPhase.Wait -> parallel {
                    onWaiting(phase.data)
                }
                is SimpleProcessPhase.Stop -> parallel{
                    SimpleProcessPhase.Stopped( onStop(phase.data) )
                }
                is SimpleProcessPhase.Stopped -> parallel{ phase }
            }
        }
}