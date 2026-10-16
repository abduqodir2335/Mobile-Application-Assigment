package uz.ttpu.shared

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice

// iosMain is shared by iosArm64 and iosSimulatorArm64, so one actual covers both.

class IOSPlatform : Platform {
    override val name: String = "${UIDevice.currentDevice.systemName()} ${iosSystemVersion()}"
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * MILLIS_PER_SECOND).toLong()

private const val MILLIS_PER_SECOND = 1000.0
