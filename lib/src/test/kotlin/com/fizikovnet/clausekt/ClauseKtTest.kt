package com.fizikovnet.clausekt

import kotlin.test.Test
import kotlin.test.assertEquals

class ClauseKtTest {
    @Test
    fun successCreateClause_defaultOperatorTest() {
        val creator = ClauseMaker()
        val filter = SimpleFilter("value_1", "value_2")
        assertEquals(
            "field1 = 'value_1' and field2 = 'value_2'",
            creator.makeClause(filter)
        )
    }

    @Test
    fun successCreateClause_likeOperatorTest() {
        val creator = ClauseMaker()
        val filter = SimpleFilter("value_1", "value_2")
        assertEquals(
            "field1 like 'value_1' and field2 like 'value_2'",
            creator.makeClause(filter, ComparisonType.LIKE)
        )
    }

    @Test
    fun successCreateEmptyClauseTest() {
        val creator = ClauseMaker()
        val filter = SimpleFilter(null, null)
        assertEquals(
            "",
            creator.makeClause(filter)
        )
    }

    @Test
    fun successCreateClauseWithLogicalOperatorTest() {
        val creator = ClauseMaker()
        val filter = SimpleFilter("value_1", "value_2")
        assertEquals(
            "field1 = 'value_1' or field2 = 'value_2'",
            creator.makeClause(filter, ComparisonType.EQUAL, LogicalType.OR)
        )
    }

    @Test
    fun successCreateClauseWithListLogicalOperatorsTest() {
        val creator = ClauseMaker()
        val filter = SimpleFilter("value_1", "value_2", "value_3")
        assertEquals(
            "field1 = 'value_1' and field2 <> 'value_2' or field3 like 'value_3'",
            creator.makeClause(filter,
                listOf(ComparisonType.EQUAL, ComparisonType.NOT_EQUAL, ComparisonType.LIKE),
                listOf(LogicalType.AND, LogicalType.OR))
        )
    }

    data class SimpleFilter(val field1: String?, val field2: String?, val field3: String? = null)
}