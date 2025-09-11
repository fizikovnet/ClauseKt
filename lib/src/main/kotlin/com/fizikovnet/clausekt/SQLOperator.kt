package com.fizikovnet.clausekt

enum class SQLOperator(val op: String) {
    LESS("<"),
    GREATER(">"),
    LESS_OR_EQUAL("<="),
    GREATER_OR_EQUAL(">="),
    EQUAL("="),
    NOT_EQUAL("<>"),
    LIKE("like"),
    ILIKE("ilike")
}