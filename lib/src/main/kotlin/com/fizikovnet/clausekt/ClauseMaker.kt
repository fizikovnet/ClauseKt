package com.fizikovnet.clausekt

import com.fizikovnet.clausekt.ComparisonType.*
import com.fizikovnet.clausekt.LogicalType.*

/**
 * ToDo
 * 1. Collections in field: List<String>
 * 2. Support filter by jsonb data column
 * 3. Exception cases in build() fun: e.g. not correct compare or logical operators were passed
 */
class ClauseMaker() {

    private var obj: Any? = null
    private var excluded: MutableList<Int> = mutableListOf()
    private var operators: List<ComparisonType> = listOf(EQUAL)
    private var logicalOps: List<LogicalType> = listOf(AND)

    constructor(obj: Any) : this() {
        this.obj = obj
    }

    fun exclude(vararg fieldIndexes: Int) = apply {
        excluded.addAll(fieldIndexes.toList())
    }

    fun operators(vararg ops: ComparisonType) = apply {
        operators = listOf(*ops)
    }

    fun binds(vararg ops: LogicalType) = apply {
        logicalOps = listOf(*ops)
    }

    fun build(): String {
        val conditions = mutableListOf<String>()
        if (obj == null) {
            throw ClauseMakerException("data class should be pass in constructor")
        }
        obj!!::class.java.declaredFields.filterIndexed { idx, _ -> !excluded.contains(idx) }
            .forEachIndexed { idx, field ->
                field.isAccessible = true
                val fieldVal = field.get(obj)
                val compOp = operators.getOrElse(idx) { operators[0] }
                conditions.add(buildString {
                    if (idx > 0) {
                        val logicOp = logicalOps.getOrNull(idx - 1) ?: logicalOps.first()
                        append(logicOp.op)
                    }
                    append("${field.name} ${compOp.op} ")
                    if (basicTypes.contains(field.type.kotlin) || fieldVal == null) {
                        append(field.get(obj))
                    } else {
                        append("'${field.get(obj)}'")
                    }
                })
            }
        return conditions.joinToString("")
    }

    @Deprecated("outdated method")
    fun makeClause(obj: Any,
                   operator: ComparisonType = ComparisonType.EQUAL,
                   logicalType: LogicalType = LogicalType.AND): String {
        val conditions = mutableListOf<String>()
        for (field in obj::class.java.declaredFields) {
            field.isAccessible = true
            field.get(obj)?.let {
                conditions.add(buildString {
                    append("${field.name} ${operator.op} ")
                    if (basicTypes.contains(field.type.kotlin)) {
                        append(field.get(obj).toString())
                    } else {
                        append("'${field.get(obj)}'")
                    }
                })
            }
        }
        return conditions.joinToString(logicalType.op)
    }

    @Deprecated("outdated method")
    fun makeClause(obj: Any, compareOperations: List<ComparisonType>, logicalBindOperations: List<LogicalType>): String {
        val conditions = mutableListOf<String>()
        val fields = obj::class.java.declaredFields
        val nonNullFields = fields.filter {
            it.isAccessible = true
            it.get(obj) != null
        }
        if (compareOperations.size != nonNullFields.size)
            throw ClauseMakerException("compareOperations size isn't equal of number of fields")
        if (logicalBindOperations.size != (nonNullFields.size-1))
            throw ClauseMakerException("logicalBindOperations should be 1 less then number of fields")
        nonNullFields.forEachIndexed { index, field ->
            field.isAccessible = true
            field.get(obj)?.let {
                conditions.add(buildString {
                    append("${field.name} ${compareOperations[index].op} ")
                    if (basicTypes.contains(field.type.kotlin)) {
                        append(field.get(obj).toString())
                    } else {
                        append("'${field.get(obj)}'")
                    }
                })
                if (index <= logicalBindOperations.lastIndex)
                    conditions.add(logicalBindOperations[index].op)
            }
        }
        return conditions.joinToString("")
    }

    companion object {
        private val basicTypes = listOf(
            Byte::class,
            Short::class,
            Int::class,
            Long::class,
            Float::class,
            Double::class,
            Boolean::class
        )
    }
}