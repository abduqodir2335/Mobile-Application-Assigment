package com.asaddeving.templatelab

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateJvmTest {
    @Test
    fun jvmSeesTheTemplateGroup() {
        assertEquals("shared by JVM and JS, running on JVM", sharedByJvmAndJs())
        assertEquals("Hello from JVM", greet())
    }
}
