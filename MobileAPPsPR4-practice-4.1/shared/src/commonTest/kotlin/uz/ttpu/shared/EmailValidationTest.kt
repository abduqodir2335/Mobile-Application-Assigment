package uz.ttpu.shared

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Task 12: runs on every target: ./gradlew :shared:jvmTest, :shared:testDebugUnitTest,
// :shared:iosSimulatorArm64Test (or :shared:allTests).
class EmailValidationTest {
    @Test
    fun acceptsValidEmail() {
        assertTrue(isValidEmail("student@ttpu.uz"))
    }

    @Test
    fun rejectsEmailWithoutDomain() {
        assertFalse(isValidEmail("student@"))
    }
}
