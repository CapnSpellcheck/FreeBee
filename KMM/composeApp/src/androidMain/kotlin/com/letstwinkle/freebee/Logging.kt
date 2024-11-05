package com.letstwinkle.freebee

actual fun Log(format: String, vararg params: Any) {
   val msg = String.format(format, *params)
}