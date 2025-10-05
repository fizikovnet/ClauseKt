package com.fizikovnet.clausekt

enum class ComparisonType(val op: String) {
    LESS("<"),
    GREATER(">"),
    LESS_OR_EQUAL("<="),
    GREATER_OR_EQUAL(">="),
    EQUAL("="),
    NOT_EQUAL("<>"),
    LIKE("like"),
    ILIKE("ilike"),
    IN("in"),
    NOT_IN("not in")
}

enum class LogicalType(val op: String) {
    AND(" and "),
    OR(" or "),
    NOT(" not ")
}