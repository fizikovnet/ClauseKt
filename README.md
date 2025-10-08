# ClauseKt - SQL WHERE Clause Builder for Kotlin

[![Gradle CI](https://github.com/fizikovnet/ClauseKt/actions/workflows/run-tests.yml/badge.svg?branch=main)](https://github.com/fizikovnet/ClauseKt/actions/workflows/run-tests.yml)

ClauseKt is a Kotlin library that automatically generates parameterized SQL WHERE clauses from data objects. It provides a safe and convenient way to build dynamic SQL queries without the risk of SQL injection attacks.

## Features

- **Automatic SQL Generation**: Convert Kotlin data classes to SQL WHERE clauses automatically
- **Parameterized Queries**: Generates safe parameterized queries to prevent SQL injection
- **Flexible Operators**: Support for various comparison operators (=``, `<>`, `<`, `>`, `<=`, `>=`, `like`, `ilike`, `in`, `not in`)
- **Logical Operators**: Support for AND, OR, NOT logical operations between conditions
- **Collection Support**: Automatically handles various collection types (List, Set, Map, and all their implementations) with IN/NOT IN clauses
- **Field Filtering**: Exclude specific fields from the generated clause
- **CamelCase Conversion**: Automatically converts camelCase field names to snake_case column names

## Installation

To use ClauseKt in your project, add it as a dependency to your build system.

## Usage

### Basic Usage

```kotlin
import com.fizikovnet.clausekt.ClauseMaker
import com.fizikovnet.clausekt.ComparisonType.*

// Define a data class
data class UserFilter(
    val name: String?,
    val age: Int?,
    val active: Boolean?
)

// Create a filter instance
val filter = UserFilter("John", 25, true)

// Generate SQL clause
val clauseMaker = ClauseMaker(filter)
val result = clauseMaker.build()

println(result.sql)        // "name = ? and age = ? and active = ?"
println(result.parameters) // ["John", 25, true]
```

### Using Different Operators

```kotlin
import com.fizikovnet.clausekt.LogicalType.*

val filter = UserFilter("John", 25, true)
val clauseMaker = ClauseMaker(filter)

// Apply different comparison operators to each field
val result = clauseMaker
    .operators(LIKE, GREATER_OR_EQUAL, EQUAL) // name LIKE, age >=, active =
    .binds(AND, OR)                           // name AND age OR active
    .build()

println(result.sql)        // "name like ? and age >= ? or active = ?"
println(result.parameters) // ["John", 25, true]
```

### Working with Collections

```kotlin
data class CollectionFilter(
    val status: List<String>?,
    val priority: Set<Int>?,
    val categories: Map<String, String>?,
    val tags: MutableList<String>?
)

val filter = CollectionFilter(
    listOf("active", "pending"), 
    setOf(1, 2), 
    mapOf("type" to "premium", "region" to "us"),
    mutableListOf("tag1", "tag2")
)
val clauseMaker = ClauseMaker(filter)

val result = clauseMaker.build()

println(result.sql)        // "status in (?, ?) and priority in (?, ?) and categories in (?, ?) and tags in (?, ?)"
println(result.parameters) // ["active", "pending", 1, 2, "premium", "us", "tag1", "tag2"] - Map values are used
```

### NOT IN Operator

```kotlin
val filter = ListFilter(listOf("inactive", "suspended"), listOf(4, 5))
val clauseMaker = ClauseMaker(filter)

// Use NOT IN operator
val result = clauseMaker
    .operators(NOT_IN, NOT_IN)
    .build()

println(result.sql)        // "status not in (?, ?) and priority not in (?, ?)"
println(result.parameters) // ["inactive", "suspended", 4, 5]
```

### Excluding Fields

```kotlin
data class ProductFilter(
    val name: String?,
    val category: String?,
    val price: Double?
)

val filter = ProductFilter("Laptop", "Electronics", 999.99)
val clauseMaker = ClauseMaker(filter)

// Exclude the category field (index 1) from the clause
val result = clauseMaker
    .exclude(1)  // Exclude category field
    .build()

println(result.sql)        // "name = ? and price = ?"
println(result.parameters) // ["Laptop", 999.99]
```

### Field Name Conversion

The library automatically converts camelCase field names to snake_case column names:

```kotlin
data class User(
    val firstName: String?,
    val lastName: String?,
    val emailAddress: String?
)

val user = User("John", "Doe", "john@example.com")
val clauseMaker = ClauseMaker(user)

val result = clauseMaker.build()

println(result.sql)        // "first_name = ? and last_name = ? and email_address = ?"
println(result.parameters) // ["John", "Doe", "john@example.com"]
```

## Security

ClauseKt generates parameterized queries to prevent SQL injection attacks. All values are properly escaped and passed as parameters rather than being directly inserted into the SQL string.

## API Reference

### ClauseMaker

- `ClauseMaker(obj: Any)`: Constructor that accepts the data object
- `operators(vararg ops: ComparisonType)`: Specify comparison operators for each field
- `binds(vararg ops: LogicalType)`: Specify logical operators between conditions
- `exclude(vararg fieldIndexes: Int)`: Exclude specific fields by index
- `build()`: Generate the SQL clause and parameters

### ComparisonType

- `EQUAL` (=)
- `NOT_EQUAL` (<>)
- `LESS` (<)
- `GREATER` (>)
- `LESS_OR_EQUAL` (<=)
- `GREATER_OR_EQUAL` (>=)
- `LIKE` (like)
- `ILIKE` (ilike)
- `IN` (in)
- `NOT_IN` (not in)

### LogicalType

- `AND` (and)
- `OR` (or)
- `NOT` (not)

## Error Handling

The library throws `ClauseMakerException` when:
- The number of comparison operators doesn't match the number of fields (when multiple operators are provided)
- The number of logical operators isn't one less than the number of fields (when multiple logical operators are provided)
- No data object is provided to the constructor
- IN or NOT_IN operators are used with primitive (non-collection) field types
