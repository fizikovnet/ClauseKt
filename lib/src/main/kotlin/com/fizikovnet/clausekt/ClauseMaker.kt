package com.fizikovnet.clausekt

import com.fizikovnet.clausekt.ComparisonType.*
import com.fizikovnet.clausekt.LogicalType.*
import java.lang.reflect.Field
import java.util.*

/**
 * ToDo
 * 1. Support filter by jsonb data column
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

        validateOperationSizes(conditions.size)
        
        return sqlGenerator.generateSql(conditions, logicalOps)
    }

    private fun validateInputData() {
        if (obj == null) throw ClauseMakerException("data class should be pass in constructor")
    }
    
    private fun validateOperationSizes(fieldCount: Int) {
        // Only validate operators size if multiple operators are provided
        // If only one operator is provided, it will be applied to all fields
        if (operators.size > 1 && operators.size != fieldCount && fieldCount > 0) {
            throw ClauseMakerException("compareOperations size isn't equal of number of fields")
        }
        // Only validate logical operators size if multiple logical operators are provided
        // If only one (default) logical operator is provided, it will be used for all connections
        if (logicalOps.size > 1 && logicalOps.size != (fieldCount - 1) && fieldCount > 1) {
            throw ClauseMakerException("logicalBindOperations should be 1 less then number of fields")
        }
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
        val operator = if (operators.size == 1) {
            operators.first()  // Use the single provided operator for all fields
        } else {
            operators.getOrNull(idx) ?: operators.first()  // Use operator at index or fallback to first
        }
        val isCollection = isValueCollectionType(fieldValue)

        // Validate that IN/NOT_IN operators are not used with primitive (non-collection) fields
        if (!isCollection && (operator == IN || operator == NOT_IN)) {
            throw ClauseMakerException("IN and NOT_IN operators can only be used with collection field types, field '${field.name}' is a primitive type")
        }

        return if (isCollection) {
            val valueToUse = when (fieldValue) {
                // For Maps, we use the values
                is Map<*, *> -> fieldValue.values.toList()
                is Set<*> -> fieldValue.toList()
                else -> fieldValue
            }

            Condition(toUnderscoreCase(field.name), operator, valueToUse, isList = true)
        } else {
            Condition(toUnderscoreCase(field.name), operator, fieldValue, isList = false)
        }
    }

    private fun isValueCollectionType(value: Any?): Boolean {
        if (value == null) return false
        
        return when (value) {
            is Collection<*>, is Map<*, *> -> true
            else -> {
                val javaClass = value.javaClass
                // Check if it's an array
                if (javaClass.isArray) return true
                
                // Check if it implements Collection or Map interfaces
                val interfaces = javaClass.interfaces
                for (interfaceType in interfaces) {
                    if (interfaceType == java.util.Collection::class.java ||
                        interfaceType == java.util.Map::class.java) {
                        return true
                    }
                }
                
                // Check class hierarchy
                var superClass = javaClass.superclass
                while (superClass != null) {
                    if (superClass == java.util.Collection::class.java ||
                        superClass == java.util.Map::class.java) {
                        return true
                    }
                    superClass = superClass.superclass
                }
                
                false
            }
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