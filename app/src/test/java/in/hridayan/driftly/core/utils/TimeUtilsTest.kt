package `in`.hridayan.driftly.core.utils

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class TimeUtilsTest {

    @Test
    fun testCalculateDuration_SameDay() {
        val duration = TimeUtils.calculateDuration("09:00", "10:30")
        assertEquals(90, duration)
    }

    @Test
    fun testCalculateDuration_MidnightCrossing() {
        val duration = TimeUtils.calculateDuration("23:00", "01:30")
        assertEquals(150, duration)
    }

    @Test
    fun testIsValidTimeRange() {
        assertTrue(TimeUtils.isValidTimeRange("09:00", "10:00"))
        assertTrue(TimeUtils.isValidTimeRange("23:00", "01:00"))
        assertFalse(TimeUtils.isValidTimeRange("09:00", "09:00"))
    }
}
