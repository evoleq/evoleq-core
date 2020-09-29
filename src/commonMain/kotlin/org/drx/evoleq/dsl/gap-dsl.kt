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

import org.drx.configuration.Configuration
import org.drx.evoleq.evolution.Gap
import org.drx.evoleq.evolving.KlEvolving

open class GapConfiguration<W, P> : Configuration<Gap<W, P>> {

    lateinit var from: KlEvolving<W, Pair<W, P>>
    lateinit var to: KlEvolving<Pair<W, P>, W>

    override fun configure(): Gap<W, P> = object : Gap<W, P> {
        override val from: KlEvolving<W, Pair<W, P>>
            get() = this@GapConfiguration.from
        override val to: KlEvolving<Pair<W, P>, W>
            get() = this@GapConfiguration.to
    }
    @EvoleqDsl
    fun GapConfiguration<W, P>.from(arrow: KlEvolving<W, Pair<W, P>>) {
        from = arrow
    }
    @EvoleqDsl
    fun GapConfiguration<W, P>.to(arrow: KlEvolving<Pair<W, P>, W>) {
        to = arrow
    }
}

@EvoleqDsl
fun <W, P > gap(configuration: GapConfiguration<W, P>.()->Unit): Gap<W, P> = with(GapConfiguration<W, P>()){
    configuration()
    configure()
}//configure(configuration)


