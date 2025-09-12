package com.fizikovnet.clausekt

class ClauseMaker {
    fun makeClause(obj: Any, operator: SQLOperator? = null): String {
        val conditions = mutableListOf<String>()
        val op = operator?.op ?: SQLOperator.EQUAL.op
        for (field in obj::class.java.declaredFields) {
            field.isAccessible = true
            field.get(obj)?.let {
                conditions.add("${field.name} $op '${field.get(obj)}'")
            }
        }
        return conditions.joinToString(" and ")
    }
}