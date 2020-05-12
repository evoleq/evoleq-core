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
package org.drx.evoleq.evolution.flows.process

import kotlinx.coroutines.CoroutineScope
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.flow
import org.drx.evoleq.evolution.Flow
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase
import org.drx.evoleq.evolution.stubs.SimpleProcessStub

@Suppress("FunctionName")
fun <Data> SimpleProcessFlow(
    onStart: suspend CoroutineScope.(Data)->Data,
    onWaiting: suspend CoroutineScope.(Data)-> SimpleProcessPhase<Data>,
    onStop: suspend CoroutineScope.(Data)->Data
): Flow<SimpleProcessPhase<Data>, Boolean> = SimpleProcessStub(
    onStart,
    onWaiting,
    onStop
).flow(
    conditions {
        testObject(true)
        check { value -> value }
        updateCondition { phase -> phase !is SimpleProcessPhase.Stopped }
    }
)