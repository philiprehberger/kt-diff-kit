package com.philiprehberger.diffkit

/**
 * The result of comparing two maps.
 *
 * @param K the key type
 * @param V the value type
 * @property added entries present in the new map but not the old
 * @property removed entries present in the old map but not the new
 * @property changed entries present in both maps with different values
 */
public data class MapDiffResult<K, V>(
    public val added: Map<K, V>,
    public val removed: Map<K, V>,
    public val changed: Map<K, Pair<V, V>>
) {
    /**
     * Returns true if there are any differences between the maps.
     */
    public fun hasChanges(): Boolean = added.isNotEmpty() || removed.isNotEmpty() || changed.isNotEmpty()
}

/**
 * Compares two maps and returns a [MapDiffResult] describing added, removed, and changed entries.
 *
 * ```
 * val result = diffMaps(
 *     mapOf("a" to 1, "b" to 2),
 *     mapOf("b" to 3, "c" to 4)
 * )
 * // result.added == { "c" to 4 }
 * // result.removed == { "a" to 1 }
 * // result.changed == { "b" to (2, 3) }
 * ```
 *
 * @param old the original map
 * @param new the modified map
 * @return a [MapDiffResult] with added, removed, and changed entries
 */
public fun <K, V> diffMaps(old: Map<K, V>, new: Map<K, V>): MapDiffResult<K, V> {
    val added = mutableMapOf<K, V>()
    val removed = mutableMapOf<K, V>()
    val changed = mutableMapOf<K, Pair<V, V>>()

    for ((key, oldValue) in old) {
        val newValue = new[key]
        if (newValue == null && key !in new) {
            removed[key] = oldValue
        } else if (newValue != oldValue) {
            @Suppress("UNCHECKED_CAST")
            changed[key] = oldValue to (newValue as V)
        }
    }

    for ((key, newValue) in new) {
        if (key !in old) {
            added[key] = newValue
        }
    }

    return MapDiffResult(added, removed, changed)
}
