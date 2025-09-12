package com.fizikovnet.clausekt

import com.fizikovnet.clausekt.SQLOperator.*

class ClauseBuilder(private val container: MutableList<Piece> = mutableListOf()) {

    private var index: Int = 0

    fun field(value: String) = apply { this.container.add(index,
        Piece(field = value)
    )}

    fun op(operator: SQLOperator) = apply {
        this.container[index].operator = operator
    }

    fun value(v: String) = apply {
        this.container[index].value = v
        index++
    }

    /**
     * ToDo if Piece.value is null - throw exception
     */
    fun build(): String {
        return container
            .map { "${it.field} ${it.operator.op} '${it.value}'" }
            .joinToString( " and " )
    }
}

data class Piece(val field: String, var value: String? = null, var operator: SQLOperator = EQUAL)