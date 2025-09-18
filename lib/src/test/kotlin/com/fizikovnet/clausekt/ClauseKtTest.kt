package com.fizikovnet.clausekt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * ToDo:
 * 1. use @ParameterizedTest and @MethodSource for test cases
 */
class ClauseKtTest {
    @Test
    fun successCreateClause_defaultOperatorTest() {
        val creator = ClauseMaker()
        val filter = StringsFilter("value_1", "value_2")
        assertEquals(
            "field1 = 'value_1' and field2 = 'value_2'",
            creator.makeClause(filter)
        )
    }

    @Test
    fun successCreateClause_likeOperatorTest() {
        val creator = ClauseMaker()
        val filter = StringsFilter("value_1", "value_2")
        assertEquals(
            "field1 like 'value_1' and field2 like 'value_2'",
            creator.makeClause(filter, ComparisonType.LIKE)
        )
    }

    @Test
    fun successCreateEmptyClauseTest() {
        val creator = ClauseMaker()
        val filter = StringsFilter(null, null)
        assertEquals(
            "",
            creator.makeClause(filter)
        )
    }

    @Test
    fun successCreateClauseWithLogicalOperatorTest() {
        val creator = ClauseMaker()
        val filter = StringsFilter("value_1", "value_2")
        assertEquals(
            "field1 = 'value_1' or field2 = 'value_2'",
            creator.makeClause(filter, ComparisonType.EQUAL, LogicalType.OR)
        )
    }

    @Test
    fun successCreateClauseWithListLogicalOperatorsTest() {
        val creator = ClauseMaker()
        val filter = StringsFilter("value_1", "value_2", "value_3")
        assertEquals(
            "field1 = 'value_1' and field2 <> 'value_2' or field3 like 'value_3'",
            creator.makeClause(filter,
                listOf(ComparisonType.EQUAL, ComparisonType.NOT_EQUAL, ComparisonType.LIKE),
                listOf(LogicalType.AND, LogicalType.OR))
        )
    }

    @Test
    fun throwExceptionWhenListCompareOpsNotEqualFieldSizeTest() {
        val creator = ClauseMaker()
        val filter = StringsFilter("value_1", "value_2", "value_3")
        val exception = assertFailsWith<ClauseMakerException>(
            block = {
                creator.makeClause(filter,
                    listOf(ComparisonType.EQUAL),
                    listOf(LogicalType.AND, LogicalType.OR))
            }
        )
        assertEquals(exception.message, "compareOperations size isn't equal of number of fields")
    }

    @Test
    fun throwExceptionWithIncorrectLogicalListSizeTest() {
        val creator = ClauseMaker()
        val filter = StringsFilter("value_1", "value_2", "value_3")
        val exception = assertFailsWith<ClauseMakerException>(
            block = {
                 creator.makeClause(filter,
                    listOf(ComparisonType.EQUAL, ComparisonType.NOT_EQUAL, ComparisonType.LIKE),
                    listOf(LogicalType.AND))
            }
        )
        assertEquals(exception.message, "logicalBindOperations should be 1 less then number of fields")
    }

    @Test
    fun successCreateClauseWithNotAllFieldsHaveValueTest() {
        val creator = ClauseMaker()
        val filter = StringsFilter("value_1", null, "value_3")
        assertEquals(
            "field1 = 'value_1' or field3 like 'value_3'",
            creator.makeClause(filter,
                listOf(ComparisonType.EQUAL, ComparisonType.LIKE),
                listOf(LogicalType.OR))
        )
    }

    @Test
    fun successCreateClause_defaultOperator_FilterVariousFieldTypesTest() {
        val creator = ClauseMaker()
        val filter = FilterVariousFieldTypes(164, true, 198.0)
        assertEquals(
            "field1 = 164 and field2 = true and field3 = 198.0",
            creator.makeClause(filter)
        )
    }

    @Test
    fun successCreateClauseWithListLogicalOperatorsAndFilterVariousFieldTypesTest() {
        val creator = ClauseMaker()
        val filter = FilterVariousFieldTypes(164, false, 178.5)
        assertEquals(
            "field1 <= 164 and field2 = false or field3 > 178.5",
            creator.makeClause(filter,
                listOf(ComparisonType.LESS_OR_EQUAL, ComparisonType.EQUAL, ComparisonType.GREATER),
                listOf(LogicalType.AND, LogicalType.OR))
        )
    }

    data class StringsFilter(val field1: String?, val field2: String?, val field3: String? = null)
    data class FilterVariousFieldTypes(val field1: Int?, val field2: Boolean?, val field3: Double?)
}