# diff-kit

[![Tests](https://github.com/philiprehberger/kt-diff-kit/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-diff-kit/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/diff-kit)](https://central.sonatype.com/artifact/com.philiprehberger/diff-kit)
[![License](https://img.shields.io/github/license/philiprehberger/kt-diff-kit)](LICENSE)

Structured diffing of Kotlin data classes and maps with change tracking.

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("com.philiprehberger:diff-kit:0.2.4")
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>diff-kit</artifactId>
    <version>0.2.4</version>
</dependency>
```

## Usage

### Data Class Diffing

```kotlin
import com.philiprehberger.diffkit.*

data class User(val name: String, val age: Int, val email: String)

val old = User("Alice", 30, "alice@example.com")
val new = User("Alice", 31, "alice@new.com")

val result = diff(old, new)
println(result.hasChanges()) // true
println(result.changedPaths()) // [age, email]
println(result)
// age: 30 -> 31
// email: alice@example.com -> alice@new.com
```

### Nested Data Classes

```kotlin
data class Address(val street: String, val city: String)
data class Person(val name: String, val address: Address)

val result = diff(
    Person("Alice", Address("Main St", "Springfield")),
    Person("Alice", Address("Main St", "Shelbyville"))
)
println(result.changedPaths()) // [address.city]
```

### Excluding Fields

```kotlin
val result = diff(old, new) {
    exclude("email", "updatedAt")
}
```

### Wildcard Pattern Exclusion

Exclude fields using wildcard patterns with `*`:

```kotlin
val result = diff(old, new) {
    // Exclude all "metadata" sub-fields
    exclude("metadata.*")

    // Exclude "updatedAt" at any depth
    exclude("*.updatedAt")
}
```

### Custom Comparators

Supply custom comparators for specific field paths:

```kotlin
val result = diff(old, new) {
    // Case-insensitive string comparison for the "name" field
    comparator("name", Comparator { a, b ->
        (a as String).lowercase().compareTo((b as String).lowercase())
    })

    // Numeric tolerance for floating-point fields
    comparator("value", Comparator { a, b ->
        val diff = (a as Double) - (b as Double)
        if (kotlin.math.abs(diff) < 0.01) 0 else diff.compareTo(0.0)
    })
}
```

### List Element-Level Diffing

Lists are compared element by element, showing individual additions, removals, and changes with their indices:

```kotlin
data class Item(val id: Int, val value: String)
data class Container(val items: List<Item>)

val old = Container(listOf(Item(1, "a"), Item(2, "b")))
val new = Container(listOf(Item(1, "a"), Item(2, "updated"), Item(3, "c")))

val result = diff(old, new)
// items[1].value: b -> updated
// items[2]: added Item(id=3, value=c)
```

### Set Diffing

Sets are compared for added and removed elements:

```kotlin
data class TaggedItem(val name: String, val tags: Set<String>)

val old = TaggedItem("item", setOf("a", "b"))
val new = TaggedItem("item", setOf("b", "c"))

val result = diff(old, new)
// tags: removed a
// tags: added c
```

### Diff Summary

Get counts of changes by type:

```kotlin
val result = diff(old, new)
val summary = result.summary()
println(summary.added)   // number of additions
println(summary.removed) // number of removals
println(summary.changed) // number of modifications
println(summary.total)   // total count
```

You can also call `summary()` on any `List<Change>`:

```kotlin
val summary = result.changes.summary()
```

### Patch Map

Convert a diff result into a map of just the changed values:

```kotlin
val patch = diff(old, new).toPatchMap()
// e.g., { "age" to 31, "email" to "alice@new.com" }
```

Removed fields appear with `null` values, added and changed fields contain the new value.

### Map Diffing

```kotlin
val result = diffMaps(
    mapOf("a" to 1, "b" to 2, "c" to 3),
    mapOf("b" to 20, "c" to 3, "d" to 4)
)
println(result.added)   // {d=4}
println(result.removed) // {a=1}
println(result.changed) // {b=(2, 20)}
```

## API

| Class / Function | Description |
|------------------|-------------|
| `diff(old, new, config)` | Compares two data class instances recursively |
| `diffMaps(old, new)` | Compares two maps for added, removed, and changed entries |
| `DiffResult` | Contains the list of `Change` objects |
| `DiffResult.hasChanges()` | Returns true if any differences were found |
| `DiffResult.changedPaths()` | Returns the list of changed property paths |
| `DiffResult.summary()` | Returns a `DiffSummary` with add/remove/change counts |
| `DiffResult.toPatchMap()` | Converts changes to a `Map<String, Any?>` of new values |
| `Change` | A single change with path, oldValue, newValue, and type |
| `ChangeType` | Enum: `CHANGED`, `ADDED`, `REMOVED` |
| `DiffSummary` | Counts: `added`, `removed`, `changed`, `total` |
| `DiffConfig.exclude()` | Excludes fields by name or wildcard pattern |
| `DiffConfig.comparator()` | Registers a custom comparator for a field path |
| `List<Change>.summary()` | Extension to produce a `DiffSummary` from any change list |
| `MapDiffResult` | Contains added, removed, and changed map entries |

## Development

```bash
./gradlew test       # Run tests
./gradlew check      # Run all checks
./gradlew build      # Build JAR
```

## License

MIT
