package com.fizikovnet.clausekt

import com.fizikovnet.clausekt.ComparisonType.*
import com.fizikovnet.clausekt.LogicalType.*
import java.lang.reflect.Field

/**
 * ToDo
 * 1. Collections in field: working with two types of compare type ops (IN, NOT IN)
 * 2. Support filter by jsonb data column
 * 3. Exception cases in build() fun: e.g. not correct compare or logical operators were passed
 */
class ClauseMaker() {

    private var obj: Any? = null
    private var excluded: MutableList<Int> = mutableListOf()
    private var operators: List<ComparisonType> = listOf(EQUAL)
    private var logicalOps: List<LogicalType> = listOf(AND)
    private val sqlGenerator = SqlGenerator()

    constructor(obj: Any) : this() {
        this.obj = obj
    }

    /**
     * ToDo make another fun which accept list of fields instead indexes
     */
    fun exclude(vararg fieldIndexes: Int) = apply {
        excluded.addAll(fieldIndexes.toList())
    }

    fun operators(vararg ops: ComparisonType) = apply {
        operators = listOf(*ops)
    }

    fun binds(vararg ops: LogicalType) = apply {
        logicalOps = listOf(*ops)
    }

    fun build(): SqlGenerator.ClauseResult {
        validateInputData()
        val conditions = mutableListOf<Condition>()
        processFieldsForConditions(conditions)
        return sqlGenerator.generateSql(conditions, logicalOps)
    }

    private fun validateInputData() {
        if (obj == null) throw ClauseMakerException("data class should be pass in constructor")
    }

    private fun processFieldsForConditions(conditions: MutableList<Condition>) {
        obj!!::class.java.declaredFields.filterIndexed { idx, _ -> !excluded.contains(idx) }
            .forEachIndexed { idx, field ->
                val condition = createCondition(field, idx)
                conditions.add(condition)
            }
    }
    
    private fun createCondition(field: Field, idx: Int): Condition {
        field.isAccessible = true
        val fieldValue = field.get(obj)
        val operator = operators.getOrNull(idx) ?: operators.first()
        
        return if (isListType(field)) {
            Condition(toUnderscoreCase(field.name), operator, fieldValue, isList = true)
        } else {
            Condition(toUnderscoreCase(field.name), operator, fieldValue, isList = false)
        }
    }

    private fun isListType(field: Field): Boolean = field.type.kotlin in listOf(List::class, Set::class)

    private fun toUnderscoreCase(str: String): String {
        return str.mapIndexed { index, c ->
            if (index > 0 && c.isUpperCase()) "_$c" else c.toString()
        }.joinToString("").lowercase()
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