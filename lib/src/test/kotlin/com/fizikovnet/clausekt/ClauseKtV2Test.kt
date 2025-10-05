package com.fizikovnet.clausekt

import kotlin.test.Test
import kotlin.test.assertEquals
import com.fizikovnet.clausekt.ComparisonType.*
import com.fizikovnet.clausekt.LogicalType.*
import kotlin.test.Ignore
import kotlin.test.assertFailsWith

class ClauseKtV2Test {

    @Test
    fun successCreateDefaultClauseTest() {
        val filter = FilterString("value_1", "value_2")
        val creator = ClauseMaker(filter)
        val result = creator.build()
        assertEquals(
            "field1 = ? and field2 = ? and complex_field_name = ?",
            result.sql
        )
        assertEquals(listOf("value_1", "value_2", null), result.parameters)
    }

    @Test
    fun successCreateClauseWithExcludeFieldsTest() {
        val filter = FilterString(null, null)
        val creator = ClauseMaker(filter)
        val result = creator.exclude(0, 2).build()
        assertEquals(
            "field2 = ?",
            result.sql
        )
        assertEquals(listOf(null), result.parameters)
    }

    @Test
    fun successCreateClauseWithSpecifyOperatorsTest() {
        val filter = FilterVariousPrimitiveFieldTypes(42, true, 164.07)
        val creator = ClauseMaker(filter)
        val result = creator
            .operators(GREATER_OR_EQUAL, EQUAL, LESS)
            .binds(OR, AND)
            .build()
        assertEquals(
            "field1 >= ? or field2 = ? and field3 < ?",
            result.sql
        )
        assertEquals(listOf(42, true, 164.07), result.parameters)
    }

    @Test
    fun successCreateClauseObjectHasListFieldTest() {
        val filter = FilterListFieldTypes(listOf("str_1", "str_2"), listOf(56, 87, 109))
        val creator = ClauseMaker(filter)
        val result = creator.build()
        assertEquals(
            "field1 in (?, ?) and field2 in (?, ?, ?)",
            result.sql
        )
        assertEquals(listOf("str_1", "str_2", 56, 87, 109), result.parameters)
    }

    @Test
    @Ignore
    fun successCreateClauseObjectHasListAndSetFieldTest() {
        val filter = FilterSetAndListFieldTypes(setOf(164, 178), listOf("v1", "v5"))
        val creator = ClauseMaker(filter)
        val result = creator.build()
        assertEquals(
            "field1 in (?, ?) and field2 in (?, ?)",
            result.sql
        )
        assertEquals(listOf(164, 178, "v1", "v5"), result.parameters)
    }

    @Test
    fun successCreateClauseWithComplexFieldNameTest() {
        val filter = FilterString("value_1", "value_2", "value_3")
        val creator = ClauseMaker(filter)
        val result = creator.build()
        assertEquals(
            "field1 = ? and field2 = ? and complex_field_name = ?",
            result.sql
        )
        assertEquals(listOf("value_1", "value_2", "value_3"), result.parameters)
    }

    @Test
    fun testCamelCaseFieldNamesConvertToUnderscore() {
        val filter = FilterCamelCase("value1", "value2", "value3")
        val creator = ClauseMaker(filter)
        val result = creator.build()
        assertEquals(
            "simple_field = ? and field_name = ? and complex_field_name = ?",
            result.sql
        )
        assertEquals(listOf("value1", "value2", "value3"), result.parameters)
    }

    @Test
    fun testSqlInjectionProtection() {
        // Test with a value that could be used for SQL injection
        val filter = FilterString("'; DROP TABLE users; --", "normal_value", null)
        val creator = ClauseMaker(filter)
        val result = creator.build()
        
        // Verify that the malicious input is not directly inserted into the SQL
        assertEquals("field1 = ? and field2 = ? and complex_field_name = ?", result.sql)
        assertEquals(listOf("'; DROP TABLE users; --", "normal_value", null), result.parameters)
        
        // The important thing is that the malicious SQL is not in the SQL string itself
        // but is safely parameterized as a parameter value
        assert(!result.sql.contains("DROP TABLE"))
    }

    @Test
    fun testNotInOperatorWithListField() {
        val filter = FilterListFieldTypes(listOf("str_1", "str_2"), listOf(56, 87, 109))
        val creator = ClauseMaker(filter)
        val result = creator.operators(IN, NOT_IN).build()
        
        assertEquals("field1 in (?, ?) and field2 not in (?, ?, ?)", result.sql)
        assertEquals(listOf("str_1", "str_2", 56, 87, 109), result.parameters)
    }

    @Test
    fun throwExceptionWhenListCompareOpsNotEqualFieldSizeTest() {
        val filter = FilterString("value_1", "value_2", "value_3")
        val creator = ClauseMaker(filter)
        val exception = assertFailsWith<ClauseMakerException> {
            creator.operators(EQUAL, NOT_EQUAL) // 2 operators for 3 fields
                .build()
        }
        assertEquals(exception.message, "compareOperations size isn't equal of number of fields")
    }

    @Test
    fun throwExceptionWithIncorrectLogicalListSizeTest() {
        val filter = FilterString("value_1", "value_2", "value_3")
        val creator = ClauseMaker(filter)
        val exception = assertFailsWith<ClauseMakerException> {
            creator.operators(EQUAL, NOT_EQUAL, LIKE)
                .binds(AND, OR, AND) // 3 logical operators for 3 fields (needs 2)
                .build()
        }
        assertEquals(exception.message, "logicalBindOperations should be 1 less then number of fields")
    }

    @Test
    fun successWithSingleOperatorForMultipleFields() {
        val filter = FilterString("value_1", "value_2", "value_3")
        val creator = ClauseMaker(filter)
        val result = creator.build() // Uses default single EQUAL operator for all fields
        
        assertEquals("field1 = ? and field2 = ? and complex_field_name = ?", result.sql)
        assertEquals(listOf("value_1", "value_2", "value_3"), result.parameters)
    }

    @Test
    fun successWithDefaultLogicalOperatorForMultipleFields() {
        val filter = FilterString("value_1", "value_2", "value_3")
        val creator = ClauseMaker(filter)
        val result = creator.operators(ComparisonType.EQUAL, ComparisonType.NOT_EQUAL, ComparisonType.LIKE) // 3 different operators
            .build() // Uses default single AND operator to connect all fields
        
        assertEquals("field1 = ? and field2 <> ? and complex_field_name like ?", result.sql)
        assertEquals(listOf("value_1", "value_2", "value_3"), result.parameters)
    }

    data class FilterString(val field1: String?, val field2: String?, val complexFieldName: String? = null)
    data class FilterCamelCase(val simpleField: String?, val fieldName: String?, val complexFieldName: String?)
    data class FilterVariousPrimitiveFieldTypes(val field1: Int?, val field2: Boolean?, val field3: Double?)
    data class FilterListFieldTypes(val field1: List<String>?, val field2: List<Int>?)
    data class FilterSetAndListFieldTypes(val field1: Set<Int>?, val field2: List<String>?)
}