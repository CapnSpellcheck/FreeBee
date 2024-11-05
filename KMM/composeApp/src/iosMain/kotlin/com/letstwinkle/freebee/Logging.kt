package com.letstwinkle.freebee

import platform.Foundation.NSLog

// TODO: Think about this, since forwarding to C varargs isn't available yet.
actual fun Log(format: String, vararg params: Any) {
//   NSLog(format, Array(params.size) { params[it] })
}