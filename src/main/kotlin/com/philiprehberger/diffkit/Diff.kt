package com.philiprehberger.diffkit

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Compares two objects of the same type and returns a [DiffResult] describing all changes.
 *
 * Uses Kotlin reflection to compare data class properties recursively. Non-data-class
 * properties are compared by equality.
 *
 * ```
 * data class User(val name: String, val age: Int)
 * val result = diff(User("Alice", 30), User("Alice", 31))
 * // result.changes == [Change("age", 30, 31)]
 * ```
 *
 * @param old the original object
 * @param new the modified object
 * @param config optional configuration block for excluding fields
 * @return a [DiffResult] containing all detected changes
 */
fun <T : Any> diff(old: T, new: T, config: DiffConfig.() -> Unit = {}): DiffResult {
    val diffConfig = DiffConfig().apply(config)
    val changes = mutableListOf<Change>()
    compareObjects(old, new, "", diffConfig.excludedFields, changes)
    return DiffResult(changes)
}

@Suppress("UNCHECKED_CAST")
private fun compareObjects(
    old: Any?,
    new: Any?,
    prefix: String,
    excludedFields: Set<String>,
    changes: MutableList<Change>
) {
    if (old === new) return
    if (old == null || new == null) {
        changes.add(Change(prefix.ifEmpty { "root" }, old, new))
        return
    }

    val klass = old::class

    // Only recurse into data classes
    if (klass.isData && klass == new::class) {
        val properties = klass.memberProperties as Collection<KProperty1<Any, *>>
        for (prop in properties) {
            val fieldName = prop.name
            if (fieldName in excludedFields) continue

            val path = if (prefix.isEmpty()) fieldName else "$prefix.$fieldName"
            val oldValue = prop.get(old)
            val newValue = prop.get(new)

            if (oldValue != newValue) {
                // Check if the value itself is a data class for recursive comparison
                if (oldValue != null && newValue != null && oldValue::class.isData && oldValue::class == newValue::class) {
                    compareObjects(oldValue, newValue, path, excludedFields, changes)
                } else {
                    changes.add(Change(path, oldValue, newValue))
                }
            }
        }
    } else if (old != new) {
        changes.add(Change(prefix.ifEmpty { "root" }, old, new))
    }
}
