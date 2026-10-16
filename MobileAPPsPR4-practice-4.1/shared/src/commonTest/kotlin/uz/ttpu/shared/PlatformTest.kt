package uz.ttpu.shared

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {
    @Test
    fun greetingNamesThePlatform() {
        assertTrue(greeting().startsWith("Hello from "))
        assertTrue(getPlatform().name.isNotBlank())
    }

    @Test
    fun clockIsAfter2020() {
        assertTrue(currentTimeMillis() > JAN_1_2020_MILLIS)
    }

    private companion object {
        const val JAN_1_2020_MILLIS = 1_577_836_800_000L
    }
}
