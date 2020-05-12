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

//import org.drx.configuration.configure
import kotlinx.coroutines.CoroutineScope
import org.drx.configuration.Configuration
import org.drx.dynamics.ID
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolution.Flow
import org.drx.evoleq.evolution.Stub
import org.drx.evoleq.evolving.DefaultEvolvingScope
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.parallel
import org.drx.evoleq.type.by


open class StubConfiguration<Data> : Configuration<Stub<out Data>> {

    protected lateinit var id: ID
    var parent: Stub<*>? = null
    val childConfigurations = hashMapOf<ID,Pair<StubConfiguration<*>,StubConfiguration<*>.()->Unit>>()

    protected val stubs: HashMap<ID, Stub<*>> by lazy { hashMapOf<ID, Stub<*>>() }
    protected lateinit var evolve: suspend CoroutineScope.(Data)-> Evolving<Data>
    protected val processes: HashMap<ID, Evolving<out Any?>> by lazy{ hashMapOf<ID, Evolving<out Any?>>() }

    override fun configure(): Stub<Data> = with(object : Stub<Data> {

        override val id: ID
            get() = this@StubConfiguration.id

        override val parent: Stub<*>?
            get() = this@StubConfiguration.parent

        override val stubs: HashMap<ID, Stub<*>>
            get() = this@StubConfiguration.stubs

        override val function: suspend CoroutineScope.(Data) -> Evolving<Data>
            get() = {data: Data ->  this@StubConfiguration.evolve(this,data)}

        //override suspend fun CoroutineScope.evolve(data: Data): Evolving<Data> = coroutineScope {  function(data) }

    }) stub@{
        configureChildren(this@stub)
        this@stub
    }

    protected open fun configureChildren(stub: Stub<Data>) {

        childConfigurations.forEach {entry -> val (id, configuration) = entry
            val newConf = configuration.first//.createInstance()
            with(configuration.second){
                newConf.this()
            }
            newConf.id = id
            newConf.parent= stub
            stubs[id] = newConf.configure()
        }
    }

    @EvoleqDsl
    fun StubConfiguration<Data>.id(id: ID) {
        this@StubConfiguration.id = id
    }
    @EvoleqDsl
    open fun StubConfiguration<Data>.evolve(arrow: suspend CoroutineScope.(Data)-> Evolving<Data>) {
        this@StubConfiguration.evolve = arrow
    }
    @EvoleqDsl
    open fun <E> StubConfiguration<Data>.child(id: ID, configuration: StubConfiguration<E>.()->Unit){
        childConfigurations[id] = Pair(StubConfiguration<E>(),configuration as StubConfiguration<*>.()->Unit)
    }
    @EvoleqDsl
    open fun <E> StubConfiguration<Data>.child(stub: Stub<E>) { child(stub.id, stub.configuration().second) }

    @EvoleqDsl
    open fun <E> StubConfiguration<Data>.child(id: ID, stub: Stub<E>) { child(id, stub.configuration().second) }

    @EvoleqDsl
    fun <P> StubConfiguration<Data>.parent(): Stub<P>? = parent as Stub<P>

    @EvoleqDsl
    fun StubConfiguration<Data>.parent(parent: Stub<*>) {
        this@StubConfiguration.parent = parent
    }

    @EvoleqDsl
    fun <E> StubConfiguration<Data>.child(id: ID): Stub<E> = this.stubs[id]!! as Stub<E>


    fun <E : Any?>   StubConfiguration<Data>.process(id: ID, data: E, scope: CoroutineScope = DefaultEvolvingScope(),process: suspend CoroutineScope.(E)->Evolving<E>) {
        scope.parallel{processes[id] = process(data)}
    }
    @EvoleqDsl
    suspend fun <E: Any> process(
        stub: Stub<E>,
        scope: CoroutineScope = DefaultEvolvingScope()
    ): (E)->Unit = {
            e -> scope.parallel { processes[stub.id] = by(stub)(e) }
        }
    @EvoleqDsl
    infix fun <E: Any> ((E)->Unit).on (data: E) = this(data)
    @EvoleqDsl
    fun <E : Any?>  StubConfiguration<Data>.process(id: ID) : Evolving<E>? = processes[id] as Evolving<E>?

}

@EvoleqDsl
fun <Data> stub(configuration: StubConfiguration<Data>.()->Unit): Stub<Data> = with(StubConfiguration<Data>()) {
    configuration()
    configure()
}

//configure(configuration) as Stub<Data>

@EvoleqDsl
fun <Data> Stub<Data>.configuration(): Pair<out StubConfiguration<*>,StubConfiguration<Data>.()->Unit> =
    Pair<StubConfiguration<*>,StubConfiguration<Data>.()->Unit>(StubConfiguration<Data>()) {
        id(this@configuration.id)
        evolve(this@configuration.function)
        stubs.putAll( this@configuration.stubs )
        when(this@configuration.parent) {
            null -> Unit
            else -> parent(this@configuration.parent!!)
        }
    }

@EvoleqDsl
fun <Data, T> Stub<Data>.flow(
    conditions: EvolutionConditions<Data, T>
): Flow<Data, T> = Flow(
    conditions,
    this
)