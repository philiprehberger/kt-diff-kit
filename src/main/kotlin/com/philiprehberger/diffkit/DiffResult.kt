package com.philiprehberger.diffkit

/**
 * The result of comparing two objects, containing all detected changes.
 *
 * @property changes the list of individual changes found
 */
data class DiffResult(val changes: List<Change>) {

    /**
     * Returns true if there are any changes between the compared objects.
     */
    fun hasChanges(): Boolean = changes.isNotEmpty()

    /**
     * Returns a list of all changed property paths.
     */
    fun changedPaths(): List<String> = changes.map { it.path }

    /**
     * Returns a human-readable summary of all changes.
     */
    override fun toString(): String {
        if (changes.isEmpty()) return "No changes"
        return changes.joinToString(separator = "\n") { "  ${it.path}: ${it.oldValue} -> ${it.newValue}" }
    }
}
