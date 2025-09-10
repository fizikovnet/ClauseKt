package com.fizikovnet.clausekt

import kotlin.test.Test
import kotlin.test.assertEquals

class ClauseKtTest {
    @Test
    fun successCreateClauseTest() {
        val creator = ClauseKt()
        val filter = SimpleFilter("value_1", "value_2")
        assertEquals(
            "field1 ilike 'value_1' and field2 ilike 'value_2'",
            creator.makeClause(filter)
        )
    }

    @Test
    fun successCreateEmptyClauseTest() {
        val creator = ClauseKt()
        val filter = SimpleFilter(null, null)
        assertEquals(
            "",
            creator.makeClause(filter)
        )
    }

    data class SimpleFilter(val field1: String?, val field2: String?)
}