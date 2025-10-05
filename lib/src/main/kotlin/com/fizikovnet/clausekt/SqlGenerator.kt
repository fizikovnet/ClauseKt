package com.fizikovnet.clausekt

data class Condition(
    val field: String,
    val operator: ComparisonType,
    val value: Any?,
    val isList: Boolean = false
)

class SqlGenerator {
    fun generateSql(conditions: List<Condition>, logicalOps: List<LogicalType> = listOf(LogicalType.AND)): ClauseResult {
        if (conditions.isEmpty()) {
            return ClauseResult("", emptyList())
        }
        
        val sqlParts = mutableListOf<String>()
        val parameters = mutableListOf<Any?>()
        
        // Add first condition
        val firstCondition = conditions[0]
        val firstSql = buildConditionSql(firstCondition, parameters)
        sqlParts.add(firstSql)
        
        // Add subsequent conditions with logical operators
        for (i in 1 until conditions.size) {
            val logicalOp = logicalOps.getOrNull(i - 1) ?: logicalOps.first()
            sqlParts.add(logicalOp.op)
            val condition = conditions[i]
            val conditionSql = buildConditionSql(condition, parameters)
            sqlParts.add(conditionSql)
        }
        
        return ClauseResult(sqlParts.joinToString(""), parameters)
    }

    data class ClauseResult(val sql: String, val parameters: List<Any?>)
    
    private fun buildConditionSql(condition: Condition, parameters: MutableList<Any?>): String {
        return if (condition.isList) {
            val listValues = condition.value as? List<*>
            if (listValues != null) {
                val placeholders = listValues.map {
                    parameters.add(it)
                    "?"
                }.joinToString(", ")
                "${condition.field} in ($placeholders)"
            } else {
                // Handle null list case
                parameters.add(condition.value)
                "${condition.field} ${condition.operator.op} ?"
            }
        } else {
            parameters.add(condition.value)
            "${condition.field} ${condition.operator.op} ?"
        }
    }
}