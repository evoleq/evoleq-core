package org.drx.evoleq.dsl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import org.drx.dynamics.Dynamic
import org.drx.evoleq.evolution.phase.process.SimpleProcessPhase
import org.drx.evoleq.evolution.stubs.ActionStub
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.parallel
import org.drx.evoleq.test.runTest
import org.evoleq.math.cat.suspend.morphism.by
import org.evoleq.math.cat.suspend.morphism.evolve
import kotlin.test.Test
import kotlin.test.assertEquals


class MyTestTest {
    @ExperimentalCoroutinesApi
    @Test fun selfUpdate() = runTest{
        val startValue = 0
        val addValue = 2
        val resultValue = startValue + addValue
        val updateCalled by Dynamic(startValue)
        val stub = actionStub<String, Int> {
            id(ActionStub::class)
            onInput { input, x ->
                when(input) {
                    "update" -> SimpleProcessPhase.Wait(selfUpdate(x){ y -> y+addValue})
                    else -> SimpleProcessPhase.Stop(x)
                }
            }
            onUpdate{
                    updated -> with(updated){
                        updateCalled.value = data
                        data
                    }
            }
        }
        var result: Evolving<Int>? = null
        parallel {
            result = evolve(startValue) by stub
        }
        stub.input("update")
        stub.input("stop")
        delay(1_500)
        
        assertEquals(result!!.get(), resultValue)
        assertEquals(updateCalled.value, resultValue)
    }
}