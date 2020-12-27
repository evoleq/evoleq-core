package org.drx.evoleq.evolving

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import org.drx.dynamics.Dynamic
import org.drx.evoleq.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class ParallelTestCommon {
    @ExperimentalTime
    @Test fun helloTest() = runTest{
        val parallel = parallel{
            delay(1_000)
        }
        val time = measureTime {
            parallel.get()
        }
        
        assertTrue(time.inMilliseconds >= 1_000L)
    }
    
    
    @ExperimentalTime
    @Test fun executionShouldStartOnInitializationOfParallel() = runTest {
        val parallel by Parallel{delay(1_000)}
        delay(1_000)
        assertTrue(
            measureTime { parallel.get() }.inMilliseconds < 1_000
        )
    }
 
    @ExperimentalTime
    @Test fun executionShouldHappenInParallel() = runTest{
        val result by Dynamic<Pair<Int,Int>?>(null)
        val time = measureTime {
            val parallel1 by Parallel {
                delay(1_000)
                1
            }
            val parallel2 by Parallel {
                delay(1_000)
                2
            }
            result.value = Pair(parallel1.get(), parallel2.get())
        }.inMilliseconds
        result.subscribe(this::class){
            it!!
            assertTrue(time >= 1_000)
            assertTrue(time <= 1_500)
            assertTrue( it.first == 1 )
            assertTrue( it.second == 2 )
        }
    }
    
    @Test fun aMappedParallelShouldTakeTheRightValue() = runTest {
        val f: suspend CoroutineScope.(Int)->String = { x -> "${x +1}"}
        val mapped by Parallel{ 0} map f
        assertTrue(mapped.get() == "1")
    }
    
    @ExperimentalTime
    @Test fun aMappedParallelShouldExecuteOnInitialization() = runTest {
        val f: suspend CoroutineScope.(Int)->String = { x -> "${x +1}"}
        val mapped by Parallel{delay(100); 0} map { x -> "${x +1}"}
        delay(100)
        assertTrue(
            measureTime { mapped.get() }.inMilliseconds < 100
        )
        delay(500)
    }
    
    
    
    @Test fun fishOperator() {
    
    }
    
  
}