package com.philiprehberger.diffkit

/**
 * Configuration for the diff operation.
 *
 * Allows excluding certain fields from comparison and providing custom comparators.
 */
public class DiffConfig {
    internal val excludedFields = mutableSetOf<String>()
    internal val excludedPatterns = mutableListOf<String>()
    internal val comparators = mutableMapOf<String, Comparator<Any?>>()

    /**
     * Excludes the specified fields from the diff comparison.
     * Supports exact field names (e.g., "email") and wildcard patterns (e.g., "*.metadata").
     *
     * Wildcard patterns use `*` to match any sequence of characters in a path segment:
     * - `"*.metadata"` matches "foo.metadata", "bar.metadata", "a.b.metadata"
     * - `"address.*"` matches "address.street", "address.city"
     * - `"*"` matches any single-segment path
     *
     * @param fields the property names or patterns to exclude
     */
    public fun exclude(vararg fields: String) {
        for (field in fields) {
            if (field.contains('*')) {
                excludedPatterns.add(field)
            } else {
                excludedFields.add(field)
            }
        }
    }

    /**
     * Registers a custom comparator for a specific field path.
     *
     * When comparing values at the given path, the provided comparator will be used
     * instead of standard equality. If the comparator returns 0, the values are
     * considered equal.
     *
     * ```
     * val result = diff(old, new) {
     *     comparator("name") { a, b ->
     *         (a as String).lowercase().compareTo((b as String).lowercase())
     *     }
     * }
     * ```
     *
     * @param path the dot-separated field path
     * @param comparator the comparator to use for values at this path
     */
    public fun comparator(path: String, comparator: Comparator<Any?>) {
        comparators[path] = comparator
    }

    /**
     * Checks whether a given field path should be excluded based on exact names or patterns.
     */
    internal fun isExcluded(path: String): Boolean {
        // Check leaf field name (backward compatibility: "email" excludes any path ending in "email")
        val leafName = path.substringAfterLast('.')
        if (leafName in excludedFields) return true

        // Check exact full path
        if (path in excludedFields) return true

        // Check wildcard patterns
        return excludedPatterns.any { pattern -> matchesWildcard(pattern, path) }
    }
}

/**
 * Matches a wildcard pattern against a path.
 * `*` matches any sequence of characters (including dots).
 */
internal fun matchesWildcard(pattern: String, path: String): Boolean {
    val regex = buildString {
        append('^')
        for (ch in pattern) {
            when (ch) {
                '*' -> append(".*")
                '.' -> append("\\.")
                else -> append(Regex.escape(ch.toString()))
            }
        }
        append('$')
    }
    return Regex(regex).matches(path)
}
