package org.drx.evoleq.debug

import org.drx.evoleq.dsl.DebugDsl

@DebugDsl
fun  log(type: String, location: String, messages: List<String>, log: (String)->Unit ) {
    var consoleOutput = "$type@$location:\n"
    consoleOutput += messages.joinToString(",\n", "    ") { it }
    val separator = "///////////////////////////////////////////////////////////////"
    consoleOutput = """
        |$separator
        |$consoleOutput
        |
        |
    """.trimMargin()
    log(consoleOutput)
}