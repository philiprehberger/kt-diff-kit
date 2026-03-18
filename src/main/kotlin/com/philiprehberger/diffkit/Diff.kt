package com.philiprehberger.diffkit

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Compares two objects of the same type and returns a [DiffResult] describing all changes.
 *
 * Uses Kotlin reflection to compare data class properties recursively. Non-data-class
 * properties are compared by equality. Lists are compared element-by-element, and sets
 * are compared for added/removed elements.
 *
 * ```
 * data class User(val name: String, val age: Int)
 * val result = diff(User("Alice", 30), User("Alice", 31))
 * // result.changes == [Change("age", 30, 31)]
 * ```
 *
 * @param old the original object
 * @param new the modified object
 * @param config optional configuration block for excluding fields and adding comparators
 * @return a [DiffResult] containing all detected changes
 */
fun <T : Any> diff(old: T, new: T, config: DiffConfig.() -> Unit = {}): DiffResult {
    val diffConfig = DiffConfig().apply(config)
    val changes = mutableListOf<Change>()
    compareObjects(old, new, "", diffConfig, changes)
    return DiffResult(changes)
}

@Suppress("UNCHECKED_CAST")
private fun compareObjects(
    old: Any?,
    new: Any?,
    prefix: String,
    config: DiffConfig,
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
            val path = if (prefix.isEmpty()) fieldName else "$prefix.$fieldName"

            if (config.isExcluded(path)) continue

            val oldValue = prop.get(old)
            val newValue = prop.get(new)

            compareValues(oldValue, newValue, path, config, changes)
        }
    } else if (old != new) {
        changes.add(Change(prefix.ifEmpty { "root" }, old, new))
    }
}

@Suppress("UNCHECKED_CAST")
private fun compareValues(
    oldValue: Any?,
    newValue: Any?,
    path: String,
    config: DiffConfig,
    changes: MutableList<Change>
) {
    // Check custom comparator first
    val customComparator = config.comparators[path]
    if (customComparator != null) {
        if (customComparator.compare(oldValue, newValue) != 0) {
            changes.add(Change(path, oldValue, newValue))
        }
        return
    }

    if (oldValue == newValue) return

    // Handle lists with element-level diffing
    if (oldValue is List<*> && newValue is List<*>) {
        compareLists(oldValue, newValue, path, config, changes)
        return
    }

    // Handle sets
    if (oldValue is Set<*> && newValue is Set<*>) {
        compareSets(oldValue, newValue, path, changes)
        return
    }

    // Recurse into data classes
    if (oldValue != null && newValue != null && oldValue::class.isData && oldValue::class == newValue::class) {
        compareObjects(oldValue, newValue, path, config, changes)
    } else {
        changes.add(Change(path, oldValue, newValue))
    }
}

/**
 * Compares two lists element by element, reporting additions, removals, and changes at each index.
 */
private fun compareLists(
    old: List<*>,
    new: List<*>,
    path: String,
    config: DiffConfig,
    changes: MutableList<Change>
) {
    val maxIndex = maxOf(old.size, new.size)
    for (i in 0 until maxIndex) {
        val elementPath = "$path[$i]"
        when {
            i >= old.size -> {
                // Element added at end
                changes.add(Change(elementPath, null, new[i], ChangeType.ADDED))
            }
            i >= new.size -> {
                // Element removed
                changes.add(Change(elementPath, old[i], null, ChangeType.REMOVED))
            }
            else -> {
                val oldVal = old[i]
                val newVal = new[i]
                if (oldVal != newVal) {
                    // If both are data classes of same type, recurse
                    if (oldVal != null && newVal != null && oldVal::class.isData && oldVal::class == newVal::class) {
                        compareObjects(oldVal, newVal, elementPath, config, changes)
                    } else {
                        changes.add(Change(elementPath, oldVal, newVal, ChangeType.CHANGED))
                    }
                }
            }
        }
    }
}

/**
 * Compares two sets, reporting added and removed elements.
 */
private fun compareSets(
    old: Set<*>,
    new: Set<*>,
    path: String,
    changes: MutableList<Change>
) {
    val added = new - old
    val removed = old - new

    for (element in removed) {
        changes.add(Change(path, element, null, ChangeType.REMOVED))
    }
    for (element in added) {
        changes.add(Change(path, null, element, ChangeType.ADDED))
    }
}
