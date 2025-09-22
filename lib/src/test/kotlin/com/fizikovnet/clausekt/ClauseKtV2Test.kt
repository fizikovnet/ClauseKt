package com.fizikovnet.clausekt

import kotlin.test.Test
import kotlin.test.assertEquals
import com.fizikovnet.clausekt.ComparisonType.*
import com.fizikovnet.clausekt.LogicalType.*

class ClauseKtV2Test {

    @Test
    fun successCreateDefaultClauseTest() {
        val filter = StringsFilter("value_1", "value_2")
        val creator = ClauseMaker(filter)
        assertEquals(
            "field1 = 'value_1' and field2 = 'value_2' and field3 = null",
            creator.build()
        )
    }

    @Test
    fun successCreateClauseWithExcludeFieldsTest() {
        val filter = StringsFilter(null, null)
        val creator = ClauseMaker(filter)
        assertEquals(
            "field2 = null",
            creator.exclude(0, 2).build()
        )
    }

    @Test
    fun successCreateClauseWithSpecifyOperatorsTest() {
        val filter = StringsFilter("value1", null, "value3")
        val creator = ClauseMaker(filter)
        assertEquals(
            "field1 like 'value1' or field2 <> null and field3 = 'value3'",
            creator
                .operators(LIKE, NOT_EQUAL, EQUAL)
                .binds(OR, AND)
                .build()
        )
    }

    data class StringsFilter(val field1: String?, val field2: String?, val field3: String? = null)
    data class FilterVariousFieldTypes(val field1: Int?, val field2: Boolean?, val field3: Double?)

}