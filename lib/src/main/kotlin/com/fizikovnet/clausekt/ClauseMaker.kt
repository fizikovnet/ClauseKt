package com.fizikovnet.clausekt

/**
 * ToDo what if not all fields in incoming object have value? e.g.: field1 = "test", field2 = null, field3 = "temp"
 */
class ClauseMaker {
    fun makeClause(obj: Any,
                   operator: ComparisonType = ComparisonType.EQUAL,
                   logicalType: LogicalType = LogicalType.AND): String {
        val conditions = mutableListOf<String>()
        for (field in obj::class.java.declaredFields) {
            field.isAccessible = true
            field.get(obj)?.let {
                conditions.add("${field.name} ${operator.op} '${field.get(obj)}'")
            }
        }
        return conditions.joinToString(logicalType.op)
    }

    fun makeClause(obj: Any, compareOperations: List<ComparisonType>, logicalBindOperations: List<LogicalType>): String {
        val conditions = mutableListOf<String>()
        val fields = obj::class.java.declaredFields
        if (compareOperations.size != fields.size)
            throw ClauseMakerException("compareOperations size isn't equal of number of fields")
        if (logicalBindOperations.size != (fields.size-1))
            throw ClauseMakerException("logicalBindOperations should be 1 less then number of fields")
        fields.forEachIndexed { index, field ->
            field.isAccessible = true
            field.get(obj)?.let {
                conditions.add("${field.name} ${compareOperations[index].op} '${field.get(obj)}'")
                if (index <= logicalBindOperations.lastIndex)
                    conditions.add(logicalBindOperations[index].op)
            }
        }
        return conditions.joinToString("")
    }
}