package org.drx.evoleq.debug

import org.drx.configuration.Configuration


expect fun Configuration<*>.info(messages: ()->List<String>,log: (String)->Unit= {println(it)})

//expect fun <Data> Stub<Data>.info(messages: ()->List<String>,log: (String)->Unit= {println(it)})