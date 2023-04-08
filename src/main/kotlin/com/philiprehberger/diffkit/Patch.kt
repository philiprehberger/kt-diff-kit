package com.philiprehberger.diffkit

/**
 * Applies a [DiffResult] to a [Map], producing a patched version of the map.
 *
 * Changed and added fields receive the new value from the diff. Removed fields are excluded.
 *
 * @param original the original map
 * @param diffResult the diff to apply
 * @return a new map with the patch applied
 */
public fun applyPatch(original: Map<String, Any?>, diffResult: DiffResult): Map<String, Any?> {
    val result = original.toMutableMap()
    for (change in diffResult.changes) {
        when (change.type) {
            ChangeType.REMOVED -> result.remove(change.path)
            ChangeType.ADDED, ChangeType.CHANGED -> result[change.path] = change.newValue
        }
    }
    return result
}
