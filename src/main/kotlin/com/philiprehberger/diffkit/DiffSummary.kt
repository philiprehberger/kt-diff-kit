package com.philiprehberger.diffkit

/**
 * A summary of changes with counts by type.
 *
 * @property added the number of additions
 * @property removed the number of removals
 * @property changed the number of modifications
 */
public data class DiffSummary(
    public val added: Int,
    public val removed: Int,
    public val changed: Int
) {
    /** The total number of changes. */
    public val total: Int get() = added + removed + changed

    override fun toString(): String = "$added added, $removed removed, $changed changed"
}

/**
 * Produces a [DiffSummary] from a list of [Change] objects.
 */
public fun List<Change>.summary(): DiffSummary {
    var added = 0
    var removed = 0
    var changed = 0
    for (change in this) {
        when (change.type) {
            ChangeType.ADDED -> added++
            ChangeType.REMOVED -> removed++
            ChangeType.CHANGED -> changed++
        }
    }
    return DiffSummary(added = added, removed = removed, changed = changed)
}
