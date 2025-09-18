package com.fizikovnet.clausekt

/**
 * ToDo
 * 1. Collections in field: List<String>
 * 2. Support filter by jsonb data column
 * 3. Corner case if param has Boolean type
 */
class ClauseMaker {
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