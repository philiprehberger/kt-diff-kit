package com.philiprehberger.diffkit

/**
 * Represents a single change detected between two objects.
 *
 * @property path the dot-separated path to the changed property (e.g., "address.city")
 * @property oldValue the value in the original object
 * @property newValue the value in the modified object
 */
data class Change(
    val path: String,
    val oldValue: Any?,
    val newValue: Any?
) {
    override fun toString(): String = "$path: $oldValue -> $newValue"
}
