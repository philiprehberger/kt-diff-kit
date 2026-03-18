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
     * Returns a [DiffSummary] with counts of added, removed, and changed entries.
     */
    fun summary(): DiffSummary = changes.summary()

    /**
     * Converts the diff result into a patch map containing only the new values
     * for changed and added fields, keyed by their dot-separated path.
     *
     * Removed fields are represented with a `null` value.
     *
     * ```
     * val patch = diff(old, new).toPatchMap()
     * // e.g., { "age" to 31, "email" to "alice@new.com" }
     * ```
     */
    fun toPatchMap(): Map<String, Any?> {
        val patch = mutableMapOf<String, Any?>()
        for (change in changes) {
            when (change.type) {
                ChangeType.REMOVED -> patch[change.path] = null
                else -> patch[change.path] = change.newValue
            }
        }
        return patch
    }

    /**
     * Returns a human-readable summary of all changes.
     */
    override fun toString(): String {
        if (changes.isEmpty()) return "No changes"
        return changes.joinToString(separator = "\n") { "  $it" }
    }
}
