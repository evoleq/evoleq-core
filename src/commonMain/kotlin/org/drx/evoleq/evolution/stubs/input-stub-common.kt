package org.drx.evoleq.evolution.stubs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.evolution.Stub
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase

expect abstract class InputStub<I,Data> : Stub<Data> {
    
    abstract val onStart: suspend CoroutineScope.(Data)->Data
    
    abstract val onInput: suspend CoroutineScope.(I, Data)-> SimpleProcessPhase<Data>
    
    
    abstract val onStop: suspend CoroutineScope.(Data)->Data
    
    @ExperimentalCoroutinesApi
    @EvoleqDsl
    suspend fun input(input: I)
    
    @ExperimentalCoroutinesApi
    @EvoleqDsl
    open fun closePorts()
}