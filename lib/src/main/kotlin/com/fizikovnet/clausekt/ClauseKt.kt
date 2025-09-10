package com.fizikovnet.clausekt

class ClauseKt {
    fun makeClause(obj: Any): String {
        val conditions = mutableListOf<String>()
        for (field in obj::class.java.declaredFields) {
            field.isAccessible = true
            field.get(obj)?.let {
                conditions.add("${field.name} ilike '${field.get(obj)}'")
            }
        }
        return conditions.joinToString(" and ")
    }
}