package com.fizikovnet.clausekt

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

    fun makeClause(obj1: Any, obj2: Any, operator: ComparisonType? = null): String {
        return ""
    }

    fun makeClause(obj: Any, operators: List<ComparisonType>): String {
        return ""
    }
}