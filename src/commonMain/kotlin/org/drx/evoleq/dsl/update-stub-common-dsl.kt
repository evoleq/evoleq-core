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
package org.drx.evoleq.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.drx.dynamics.Dynamic
import org.drx.dynamics.ID
import org.drx.evoleq.evolution.stubs.Update
import org.drx.evoleq.evolution.stubs.UpdateStub
import org.drx.evoleq.evolution.stubs.Updated
import org.drx.evoleq.evolving.Evolving

expect open class UpdateStubConfiguration<Data> : StubConfiguration<Data> {

    class UpdateStubConfigurationException(message: String?) : Exception

    var updateParent: suspend (Update<Data>)->Unit

    var onUpdate: suspend CoroutineScope.(Updated<Data>) -> Data
    
    var onStart: suspend CoroutineScope.(Data)->Data

    var onStop: suspend CoroutineScope.(Data)->Data

    internal val stub: Dynamic<UpdateStub<Data>?>

    override fun configure(): UpdateStub<Data>
    
    @EvoleqDsl
    override fun StubConfiguration<Data>.evolve(arrow: suspend CoroutineScope.(Data) -> Evolving<Data>)

    @EvoleqDsl
    fun UpdateStubConfiguration<Data>.onStart(onStart: suspend CoroutineScope.(Data)->Data)

    @EvoleqDsl
    fun UpdateStubConfiguration<Data>.onStop(onStop: suspend CoroutineScope.(Data)->Data)
    
    @EvoleqDsl
    fun UpdateStubConfiguration<Data>.onUpdate(update: suspend CoroutineScope.(Updated<Data>) -> Data)

    @ExperimentalCoroutinesApi
    @EvoleqDsl
    fun <P> UpdateStubConfiguration<Data>.updateParentBy(setter:suspend  (suspend CoroutineScope.(Data) -> Data)->(suspend CoroutineScope.(P)->P))

    @EvoleqDsl
    suspend fun <P> UpdateStubConfiguration<Data>.updateParent(update: suspend CoroutineScope.(Data)->Data)

    @ExperimentalCoroutinesApi
    @EvoleqDsl
    suspend fun <E> UpdateStubConfiguration<Data>.updateChild(childId: ID,  update: suspend CoroutineScope.(E)->E)
    
    @EvoleqDsl
    fun <E> UpdateStubConfiguration<Data>.updatableChild(id: ID, configuration: UpdateStubConfiguration<E>.() -> Unit)
    
    @EvoleqDsl
    open fun <E> UpdateStubConfiguration<Data>.updatableChild(stub: UpdateStub<E>)

    suspend fun UpdateStubConfiguration<Data>.stub(): UpdateStub<Data>

    @Suppress("unchecked_cast")
    suspend fun <P> parentalUpdateStub():  UpdateStub<P>
}

@EvoleqDsl
expect fun <Data> updateStub(configuration: UpdateStubConfiguration<Data>.()->Unit): UpdateStub<Data>

@EvoleqDsl
expect fun <E> StubConfiguration<*>.updatableChild(childId: ID,configuration: UpdateStubConfiguration<E>.()->Unit)

@EvoleqDsl
fun <E> StubConfiguration<*>.updatableChild(stub: UpdateStub<E>) { updatableChild(stub.id, stub.configuration().second) }

@EvoleqDsl
expect fun <Data> UpdateStub<Data>.configuration(): Pair<out StubConfiguration<*>,UpdateStubConfiguration<Data>.()->Unit>