package com.fizikovnet.clausekt

import com.fizikovnet.clausekt.SQLOperator.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ClauseKtBuilderTest {

    @Test
    fun testMethod() {
        val clause = ClauseBuilder()
            .field("field1").op(LIKE).value("value_1")
            .field("field2").op(EQUAL).value("value_2")
            .build()

        assertEquals(
            "field1 like 'value_1' and field2 = 'value_2'",
            clause
        )
    }
}