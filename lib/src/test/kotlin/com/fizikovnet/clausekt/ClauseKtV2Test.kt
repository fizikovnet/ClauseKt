package com.fizikovnet.clausekt

import kotlin.test.Test
import kotlin.test.assertEquals
import com.fizikovnet.clausekt.ComparisonType.*
import com.fizikovnet.clausekt.LogicalType.*
import kotlin.test.Ignore

class ClauseKtV2Test {

    @Test
    fun successCreateDefaultClauseTest() {
        val filter = FilterString("value_1", "value_2")
        val creator = ClauseMaker(filter)
        assertEquals(
            "field1 = 'value_1' and field2 = 'value_2' and complex_field_name = null",
            creator.build()
        )
    }

    @Test
    fun successCreateClauseWithExcludeFieldsTest() {
        val filter = FilterString(null, null)
        val creator = ClauseMaker(filter)
        assertEquals(
            "field2 = null",
            creator.exclude(0, 2).build()
        )
    }

    @Test
    fun successCreateClauseWithSpecifyOperatorsTest() {
        val filter = FilterVariousPrimitiveFieldTypes(42, true, 164.07)
        val creator = ClauseMaker(filter)
        assertEquals(
            "field1 >= 42 or field2 = true and field3 < 164.07",
            creator
                .operators(GREATER_OR_EQUAL, EQUAL, LESS)
                .binds(OR, AND)
                .build()
        )
    }

    @Test
    fun successCreateClauseObjectHasListFieldTest() {
        val filter = FilterListFieldTypes(listOf("str_1", "str_2"), listOf(56, 87, 109))
        val creator = ClauseMaker(filter)
        assertEquals(
            "field1 in ('str_1', 'str_2') and field2 in (56, 87, 109)",
            creator.build()
        )
    }

    @Test
    @Ignore
    fun successCreateClauseObjectHasListAndSetFieldTest() {
        val filter = FilterSetAndListFieldTypes(setOf(164, 178), listOf("v1", "v5"))
        val creator = ClauseMaker(filter)
        assertEquals(
            "field1 in (164, 178) and field2 in ('v1', 'v5')",
            creator.build()
        )
    }

    @Test
    fun successCreateClauseWithComplexFieldNameTest() {
        val filter = FilterString("value_1", "value_2", "value_3")
        val creator = ClauseMaker(filter)
        assertEquals(
            "field1 = 'value_1' and field2 = 'value_2' and complex_field_name = 'value_3'",
            creator.build()
        )
    }

    @Test
    fun testCamelCaseFieldNamesConvertToUnderscore() {
        val filter = FilterCamelCase("value1", "value2", "value3")
        val creator = ClauseMaker(filter)
        assertEquals(
            "simple_field = 'value1' and field_name = 'value2' and complex_field_name = 'value3'",
            creator.build()
        )
    }

    data class FilterString(val field1: String?, val field2: String?, val complexFieldName: String? = null)
    data class FilterCamelCase(val simpleField: String?, val fieldName: String?, val complexFieldName: String?)
    data class FilterVariousPrimitiveFieldTypes(val field1: Int?, val field2: Boolean?, val field3: Double?)
    data class FilterListFieldTypes(val field1: List<String>?, val field2: List<Int>?)
    data class FilterSetAndListFieldTypes(val field1: Set<Int>?, val field2: List<String>?)
}