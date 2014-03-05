package com.philiprehberger.diffkit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiffKitTest {

    data class User(val name: String, val age: Int, val email: String)

    data class Address(val street: String, val city: String)
    data class Person(val name: String, val address: Address)

    @Test
    fun `flat data class diff detects changed fields`() {
        val old = User("Alice", 30, "alice@example.com")
        val new = User("Alice", 31, "alice@new.com")

        val result = diff(old, new)
        assertTrue(result.hasChanges())
        assertEquals(2, result.changes.size)
        assertTrue(result.changedPaths().contains("age"))
        assertTrue(result.changedPaths().contains("email"))

        val ageChange = result.changes.first { it.path == "age" }
        assertEquals(30, ageChange.oldValue)
        assertEquals(31, ageChange.newValue)
    }

    @Test
    fun `nested data class diff detects deep changes`() {
        val old = Person("Alice", Address("123 Main St", "Springfield"))
        val new = Person("Alice", Address("123 Main St", "Shelbyville"))

        val result = diff(old, new)
        assertTrue(result.hasChanges())
        assertEquals(1, result.changes.size)
        assertEquals("address.city", result.changes[0].path)
        assertEquals("Springfield", result.changes[0].oldValue)
        assertEquals("Shelbyville", result.changes[0].newValue)
    }

    @Test
    fun `identical objects return empty diff`() {
        val obj = User("Alice", 30, "alice@example.com")
        val result = diff(obj, obj.copy())
        assertFalse(result.hasChanges())
        assertEquals(emptyList(), result.changes)
    }

    @Test
    fun `excluded fields are not compared`() {
        val old = User("Alice", 30, "alice@example.com")
        val new = User("Alice", 31, "alice@new.com")

        val result = diff(old, new) {
            exclude("email")
        }

        assertEquals(1, result.changes.size)
        assertEquals("age", result.changes[0].path)
    }

    @Test
    fun `map diff detects added entries`() {
        val old = mapOf("a" to 1, "b" to 2)
        val new = mapOf("a" to 1, "b" to 2, "c" to 3)

        val result = diffMaps(old, new)
        assertEquals(mapOf("c" to 3), result.added)
        assertTrue(result.removed.isEmpty())
        assertTrue(result.changed.isEmpty())
    }

    @Test
    fun `map diff detects removed entries`() {
        val old = mapOf("a" to 1, "b" to 2)
        val new = mapOf("a" to 1)

        val result = diffMaps(old, new)
        assertTrue(result.added.isEmpty())
        assertEquals(mapOf("b" to 2), result.removed)
        assertTrue(result.changed.isEmpty())
    }

    @Test
    fun `map diff detects changed entries`() {
        val old = mapOf("a" to 1, "b" to 2)
        val new = mapOf("a" to 1, "b" to 5)

        val result = diffMaps(old, new)
        assertTrue(result.added.isEmpty())
        assertTrue(result.removed.isEmpty())
        assertEquals(mapOf("b" to (2 to 5)), result.changed)
    }

    @Test
    fun `map diff detects added, removed, and changed together`() {
        val old = mapOf("a" to 1, "b" to 2, "c" to 3)
        val new = mapOf("b" to 20, "c" to 3, "d" to 4)

        val result = diffMaps(old, new)
        assertEquals(mapOf("d" to 4), result.added)
        assertEquals(mapOf("a" to 1), result.removed)
        assertEquals(mapOf("b" to (2 to 20)), result.changed)
        assertTrue(result.hasChanges())
    }

    @Test
    fun `identical maps have no changes`() {
        val map = mapOf("a" to 1, "b" to 2)
        val result = diffMaps(map, map)
        assertFalse(result.hasChanges())
    }

    @Test
    fun `toString produces human-readable output`() {
        val old = User("Alice", 30, "alice@example.com")
        val new = User("Bob", 30, "alice@example.com")

        val result = diff(old, new)
        val output = result.toString()
        assertTrue(output.contains("name"))
        assertTrue(output.contains("Alice"))
        assertTrue(output.contains("Bob"))
    }

    @Test
    fun `no changes toString returns descriptive message`() {
        val obj = User("Alice", 30, "alice@example.com")
        val result = diff(obj, obj.copy())
        assertEquals("No changes", result.toString())
    }
}
