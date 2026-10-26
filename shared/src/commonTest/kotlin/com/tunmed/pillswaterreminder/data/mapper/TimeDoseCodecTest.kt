package com.tunmed.pillswaterreminder.data.mapper

import com.tunmed.pillswaterreminder.domain.model.TimeDose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalTime

class TimeDoseCodecTest {

    @Test
    fun round_trip_keeps_times_amounts_and_texts() {
        val doses = mapOf(
            LocalTime(20, 0) to TimeDose(text = "1 tablet · 500 mg", amount = 1.0),
            LocalTime(8, 0) to TimeDose(text = "2 tablets, with food"),
            LocalTime(14, 30) to TimeDose(text = "0.5 tablet", amount = 0.5),
        )
        assertEquals(doses, doses.encodeTimeDoses().decodeTimeDoses())
    }

    @Test
    fun same_dose_at_every_time_is_stored_as_null() {
        assertNull(emptyMap<LocalTime, TimeDose>().encodeTimeDoses())
        val none: String? = null
        assertTrue(none.decodeTimeDoses().isEmpty())
    }

    @Test
    fun tabs_and_line_breaks_in_a_dose_become_spaces() {
        val doses = mapOf(LocalTime(8, 0) to TimeDose(text = "2 tablets\twith\nfood"))
        assertEquals("2 tablets with food", doses.encodeTimeDoses().decodeTimeDoses().getValue(LocalTime(8, 0)).text)
    }
}
