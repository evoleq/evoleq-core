package org.drx.evoleq.debug

import org.drx.configuration.Configuration
import org.drx.evoleq.dsl.DebugDsl

@DebugDsl
actual fun  Configuration<*>.info(messages: ()->List<String>, log: (String)->Unit) =
    log(
    "info",
    this::class.simpleName!!,
    messages()
    
    ) {
        log(it)
    }

@DebugDsl
fun Configuration<*>.infoConsole(messages: ()->List<String>) = info(messages){ console.log(it) }