package org.drx.evoleq.test

import kotlinx.coroutines.CoroutineScope

expect fun runTest(block: suspend CoroutineScope.()->Unit)