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

    /**
     * ToDo throw exception if compOps.size != fields.size, and logicOps.size != fields.size-1
     */
    fun makeClause(obj: Any, compOps: List<ComparisonType>, logicOps: List<LogicalType>): String {
        val conditions = mutableListOf<String>()
        obj::class.java.declaredFields.forEachIndexed { index, field ->
            field.isAccessible = true
            field.get(obj)?.let {
                conditions.add("${field.name} ${compOps[index].op} '${field.get(obj)}'")
                if (logicOps.getOrNull(index) != null) {
                    conditions.add(logicOps[index].op)
                }
            }
        }
        return conditions.joinToString("")
    }
}