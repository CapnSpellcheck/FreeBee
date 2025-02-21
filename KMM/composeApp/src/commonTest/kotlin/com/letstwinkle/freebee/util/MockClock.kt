package com.letstwinkle.freebee.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class MockClock(val now: Instant) : Clock {
   override fun now(): Instant = now
}
