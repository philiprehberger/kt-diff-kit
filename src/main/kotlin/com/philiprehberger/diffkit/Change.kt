package com.philiprehberger.diffkit

/**
 * The type of change detected.
 */
enum class ChangeType {
    /** A value was modified from one value to another. */
    CHANGED,
    /** A new value was added (e.g., list element, set element). */
    ADDED,
    /** A value was removed (e.g., list element, set element). */
    REMOVED
}

/**
 * Represents a single change detected between two objects.
 *
 * @property path the dot-separated path to the changed property (e.g., "address.city")
 * @property oldValue the value in the original object
 * @property newValue the value in the modified object
 * @property type the type of change (CHANGED, ADDED, or REMOVED)
 */
data class Change(
    val path: String,
    val oldValue: Any?,
    val newValue: Any?,
    val type: ChangeType = ChangeType.CHANGED
) {
    override fun toString(): String = when (type) {
        ChangeType.ADDED -> "$path: added $newValue"
        ChangeType.REMOVED -> "$path: removed $oldValue"
        ChangeType.CHANGED -> "$path: $oldValue -> $newValue"
    }
}
