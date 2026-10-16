package uz.ttpu.shared

class DesktopPlatform : Platform {
    override val name: String = "Desktop (${System.getProperty("os.name")}, Java ${System.getProperty("java.version")})"
}

actual fun getPlatform(): Platform = DesktopPlatform()

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
