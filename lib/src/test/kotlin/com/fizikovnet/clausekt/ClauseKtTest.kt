package com.fizikovnet.clausekt

import kotlin.test.Test
import kotlin.test.assertEquals

class ClauseKtTest {
    @Test
    fun successCreateClause_defaultOperatorTest() {
        val creator = ClauseKt()
        val filter = SimpleFilter("value_1", "value_2")
        assertEquals(
            "field1 = 'value_1' and field2 = 'value_2'",
            creator.makeClause(filter)
        )
    }

    @Test
    fun successCreateClause_likeOperatorTest() {
        val creator = ClauseKt()
        val filter = SimpleFilter("value_1", "value_2")
        assertEquals(
            "field1 like 'value_1' and field2 like 'value_2'",
            creator.makeClause(filter, SQLOperator.LIKE)
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