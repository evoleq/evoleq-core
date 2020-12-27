package org.drx.evoleq.evolution.stubs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.drx.dynamics.ID
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.evolution.Stub
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by


data class Updated<Data> (val senderId : ID, val data: Data)

interface Update<Data> : ScopedSuspended<Data, Updated<Data>>

suspend fun <Data> Update<Data>.data(): ScopedSuspended<Data,Data> = ScopedSuspended { data -> by(this@data)(this,data).data }

@Suppress("FunctionName")
fun<Data> Update(senderId: ID, update: suspend CoroutineScope.(Data) -> Data) = object :
    Update<Data> {
    override val morphism: suspend CoroutineScope.(Data) -> Updated<Data> = {
            data ->
        Updated(senderId, update(data))
    }
}

expect abstract class UpdateStub<Data>(updateParent: suspend (Update<Data>)->Unit = {}) : Stub<Data> {
    
    abstract val onStart: suspend CoroutineScope.(Data)->Data
    
    abstract val onUpdate : suspend CoroutineScope.(Updated<Data>)->Data
    
    abstract val onStop: suspend CoroutineScope.(Data)->Data
    
    @ExperimentalCoroutinesApi
    @EvoleqDsl
    suspend fun stop()
    
    @ExperimentalCoroutinesApi
    @EvoleqDsl
    suspend fun update(senderId:ID, update: suspend CoroutineScope.(Data)->Data)
    
    @ExperimentalCoroutinesApi
    @EvoleqDsl
    suspend fun <E> updateChild(childId: ID,  update: suspend CoroutineScope.(E)->E)
    
    @EvoleqDsl
    suspend fun <P> updateParent(update: suspend CoroutineScope.(Data)->Data)
    
    @ExperimentalCoroutinesApi
    @EvoleqDsl
    open fun closePorts()
}
