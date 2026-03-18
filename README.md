# kt-diff-kit

[![CI](https://github.com/philiprehberger/kt-diff-kit/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-diff-kit/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/diff-kit)](https://central.sonatype.com/artifact/com.philiprehberger/diff-kit)

Structured diffing of Kotlin data classes and maps with change tracking.

## Requirements

- Kotlin 1.9+ / Java 17+

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.philiprehberger:diff-kit:0.1.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.philiprehberger:diff-kit:0.1.0'
}
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>diff-kit</artifactId>
    <version>0.1.0</version>
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
| `Change` | A single property change with path, oldValue, newValue |
| `DiffConfig.exclude()` | Excludes specified fields from comparison |
| `MapDiffResult` | Contains added, removed, and changed map entries |

## Development

```bash
./gradlew test       # Run tests
./gradlew check      # Run all checks
./gradlew build      # Build JAR
```

## License

MIT
