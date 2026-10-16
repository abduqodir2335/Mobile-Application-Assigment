package uz.ttpu.shared

import platform.UIKit.UIDevice

// Task 8: UIKit is an Apple framework. Only source sets that compile exclusively to Apple
// targets (iosMain, appleMain, ...) can import platform.UIKit. commonMain also compiles to
// Android and the JVM, where UIKit does not exist.
fun iosSystemVersion(): String = UIDevice.currentDevice.systemVersion
