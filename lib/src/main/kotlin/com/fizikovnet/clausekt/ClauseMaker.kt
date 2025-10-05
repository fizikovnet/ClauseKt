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
    private lateinit var conditions: MutableList<String>

    constructor(obj: Any) : this() {
        this.obj = obj
        //ToDo clear conditions var after build or make ClauseMaker object not valid for reusable
        this.conditions = mutableListOf<String>()
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

    fun build(): String {
        validateInputData()
        processFields()
        return conditions.joinToString("")
    }

    private fun validateInputData() {
        if (obj == null) throw ClauseMakerException("data class should be pass in constructor")
    }

    private fun processFields() {
        obj!!::class.java.declaredFields.filterIndexed { idx, _ -> !excluded.contains(idx) }
            .forEachIndexed { idx, field -> handleFieldOrList(field, idx) }
    }

    private fun handleFieldOrList(field: Field, idx: Int) {
        field.isAccessible = true
        if (idx > 0) {
            val logicOp = logicalOps.getOrNull(idx - 1) ?: logicalOps.first()
            conditions.add(logicOp.op)
        }
        if (isListType(field)) {
            handleListField(field)
        } else {
            handleNonListField(field, operators.getOrNull(idx) ?: operators.first())
        }
    }

    private fun isListType(field: Field): Boolean = field.type.kotlin in listOf(List::class, Set::class)

    private fun handleNonListField(field: Field, operator: ComparisonType) {
        conditions += "${toUnderscoreCase(field.name)} ${operator.op} ${getValueAsSqlValueOrReference(field)}"
    }

    private fun handleListField(field: Field) {
        val fieldVals = (field.get(obj) as List<*>).map {
            if (it == null || basicTypes.contains(it.javaClass.kotlin)) {
                it
            } else {
                "'$it'"
            }
        }.joinToString(", ")
        conditions += "${toUnderscoreCase(field.name)} in ($fieldVals)"
    }

    private fun getValueAsSqlValueOrReference(field: Field): String {
        val fieldVal = field.get(obj)
        return when {
            fieldVal == null -> "null"
            basicTypes.contains(field.type.kotlin) -> fieldVal.toString()
            else -> "'$fieldVal'"
        }
    }

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