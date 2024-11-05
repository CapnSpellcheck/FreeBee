package com.letstwinkle.freebee

import com.letstwinkle.freebee.database.CoreDataDatabase
import com.letstwinkle.freebee.database.FreeBeeRepository

actual fun repository(): FreeBeeRepository {
   return CoreDataDatabase.shared
}