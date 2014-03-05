package com.philiprehberger.diffkit

/**
 * Configuration for the diff operation.
 *
 * Allows excluding certain fields from comparison.
 */
class DiffConfig {
    internal val excludedFields = mutableSetOf<String>()

    /**
     * Excludes the specified fields from the diff comparison.
     *
     * @param fields the property names to exclude
     */
    fun exclude(vararg fields: String) {
        excludedFields.addAll(fields)
    }
}
