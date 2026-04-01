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

    // --- List element-level diffing ---

    data class Item(val id: Int, val value: String)
    data class Container(val items: List<Item>)

    @Test
    fun `list diff detects items added at end`() {
        val old = Container(listOf(Item(1, "a"), Item(2, "b")))
        val new = Container(listOf(Item(1, "a"), Item(2, "b"), Item(3, "c")))

        val result = diff(old, new)
        assertTrue(result.hasChanges())
        assertEquals(1, result.changes.size)
        val change = result.changes[0]
        assertEquals("items[2]", change.path)
        assertEquals(null, change.oldValue)
        assertEquals(Item(3, "c"), change.newValue)
        assertEquals(ChangeType.ADDED, change.type)
    }

    @Test
    fun `list diff detects items removed from middle`() {
        val old = Container(listOf(Item(1, "a"), Item(2, "b"), Item(3, "c")))
        val new = Container(listOf(Item(1, "a"), Item(3, "c")))

        val result = diff(old, new)
        assertTrue(result.hasChanges())
        // index 1: old=Item(2,"b"), new=Item(3,"c") -> data class fields changed
        val changesAtIndex1 = result.changes.filter { it.path.startsWith("items[1]") }
        assertTrue(changesAtIndex1.isNotEmpty())

        // index 2: old=Item(3,"c"), new=null -> removed
        val change2 = result.changes.first { it.path == "items[2]" }
        assertEquals(ChangeType.REMOVED, change2.type)
    }

    @Test
    fun `list diff detects changed items in place`() {
        val old = Container(listOf(Item(1, "a"), Item(2, "b")))
        val new = Container(listOf(Item(1, "a"), Item(2, "updated")))

        val result = diff(old, new)
        assertTrue(result.hasChanges())
        // Items are data classes, so we get a deep diff at items[1].value
        assertEquals(1, result.changes.size)
        assertEquals("items[1].value", result.changes[0].path)
        assertEquals("b", result.changes[0].oldValue)
        assertEquals("updated", result.changes[0].newValue)
    }

    @Test
    fun `list diff with primitive elements`() {
        data class Numbers(val values: List<Int>)

        val old = Numbers(listOf(1, 2, 3))
        val new = Numbers(listOf(1, 4, 3, 5))

        val result = diff(old, new)
        assertTrue(result.hasChanges())

        val changed = result.changes.first { it.path == "values[1]" }
        assertEquals(2, changed.oldValue)
        assertEquals(4, changed.newValue)
        assertEquals(ChangeType.CHANGED, changed.type)

        val added = result.changes.first { it.path == "values[3]" }
        assertEquals(null, added.oldValue)
        assertEquals(5, added.newValue)
        assertEquals(ChangeType.ADDED, added.type)
    }

    // --- Set diffing ---

    data class TaggedItem(val name: String, val tags: Set<String>)

    @Test
    fun `set diff detects added elements`() {
        val old = TaggedItem("item", setOf("a", "b"))
        val new = TaggedItem("item", setOf("a", "b", "c"))

        val result = diff(old, new)
        assertTrue(result.hasChanges())
        assertEquals(1, result.changes.size)
        assertEquals("tags", result.changes[0].path)
        assertEquals(null, result.changes[0].oldValue)
        assertEquals("c", result.changes[0].newValue)
        assertEquals(ChangeType.ADDED, result.changes[0].type)
    }

    @Test
    fun `set diff detects removed elements`() {
        val old = TaggedItem("item", setOf("a", "b", "c"))
        val new = TaggedItem("item", setOf("a"))

        val result = diff(old, new)
        assertTrue(result.hasChanges())
        assertEquals(2, result.changes.size)
        assertTrue(result.changes.all { it.type == ChangeType.REMOVED })
        assertTrue(result.changes.all { it.path == "tags" })
        val removedValues = result.changes.map { it.oldValue }.toSet()
        assertEquals(setOf("b", "c"), removedValues)
    }

    @Test
    fun `set diff detects added and removed together`() {
        val old = TaggedItem("item", setOf("a", "b"))
        val new = TaggedItem("item", setOf("b", "c"))

        val result = diff(old, new)
        assertTrue(result.hasChanges())
        assertEquals(2, result.changes.size)

        val removed = result.changes.first { it.type == ChangeType.REMOVED }
        assertEquals("a", removed.oldValue)

        val added = result.changes.first { it.type == ChangeType.ADDED }
        assertEquals("c", added.newValue)
    }

    // --- DiffSummary ---

    @Test
    fun `summary counts changes correctly`() {
        val changes = listOf(
            Change("a", 1, 2, ChangeType.CHANGED),
            Change("b", null, 3, ChangeType.ADDED),
            Change("c", null, 4, ChangeType.ADDED),
            Change("d", 5, null, ChangeType.REMOVED)
        )

        val summary = changes.summary()
        assertEquals(2, summary.added)
        assertEquals(1, summary.removed)
        assertEquals(1, summary.changed)
        assertEquals(4, summary.total)
    }

    @Test
    fun `DiffResult summary works`() {
        val old = TaggedItem("item", setOf("a", "b"))
        val new = TaggedItem("item", setOf("b", "c"))

        val result = diff(old, new)
        val summary = result.summary()
        assertEquals(1, summary.added)
        assertEquals(1, summary.removed)
        assertEquals(0, summary.changed)
    }

    @Test
    fun `empty changes produce zero summary`() {
        val summary = emptyList<Change>().summary()
        assertEquals(0, summary.added)
        assertEquals(0, summary.removed)
        assertEquals(0, summary.changed)
        assertEquals(0, summary.total)
    }

    // --- Custom comparators ---

    @Test
    fun `custom comparator for case-insensitive string comparison`() {
        val old = User("Alice", 30, "alice@example.com")
        val new = User("ALICE", 30, "alice@example.com")

        // Without custom comparator, names differ
        val resultDefault = diff(old, new)
        assertTrue(resultDefault.hasChanges())

        // With case-insensitive comparator, names are equal
        val resultCustom = diff(old, new) {
            comparator("name", Comparator { a, b ->
                (a as String).lowercase().compareTo((b as String).lowercase())
            })
        }
        assertFalse(resultCustom.hasChanges())
    }

    @Test
    fun `custom comparator still reports difference when values differ`() {
        val old = User("Alice", 30, "alice@example.com")
        val new = User("Bob", 30, "alice@example.com")

        val result = diff(old, new) {
            comparator("name", Comparator { a, b ->
                (a as String).lowercase().compareTo((b as String).lowercase())
            })
        }
        assertTrue(result.hasChanges())
        assertEquals(1, result.changes.size)
        assertEquals("name", result.changes[0].path)
    }

    @Test
    fun `custom comparator with numeric tolerance`() {
        data class Measurement(val value: Double, val unit: String)

        val old = Measurement(10.0, "kg")
        val new = Measurement(10.001, "kg")

        // With a tolerance comparator, small differences are ignored
        val result = diff(old, new) {
            comparator("value", Comparator { a, b ->
                val diff = (a as Double) - (b as Double)
                if (kotlin.math.abs(diff) < 0.01) 0 else diff.compareTo(0.0)
            })
        }
        assertFalse(result.hasChanges())
    }

    // --- Wildcard pattern exclusion ---

    data class Metadata(val createdAt: String, val updatedAt: String)
    data class Document(val title: String, val metadata: Metadata)
    data class Record(val name: String, val metadata: Metadata, val notes: String)

    @Test
    fun `wildcard exclusion matches trailing field`() {
        val old = Document("Doc", Metadata("2025-01-01", "2025-01-01"))
        val new = Document("Doc", Metadata("2025-01-01", "2025-06-15"))

        // Exclude any path ending with "updatedAt"
        val result = diff(old, new) {
            exclude("*.updatedAt")
        }
        assertFalse(result.hasChanges())
    }

    @Test
    fun `wildcard exclusion with prefix pattern`() {
        val old = Document("Doc", Metadata("2025-01-01", "2025-01-01"))
        val new = Document("Updated", Metadata("2025-06-01", "2025-06-15"))

        // Exclude all metadata sub-fields
        val result = diff(old, new) {
            exclude("metadata.*")
        }
        assertTrue(result.hasChanges())
        assertEquals(1, result.changes.size)
        assertEquals("title", result.changes[0].path)
    }

    @Test
    fun `wildcard exclusion does not affect non-matching fields`() {
        val old = User("Alice", 30, "alice@example.com")
        val new = User("Alice", 31, "alice@new.com")

        val result = diff(old, new) {
            exclude("*.metadata")
        }
        // No metadata field exists, so nothing is excluded
        assertEquals(2, result.changes.size)
    }

    @Test
    fun `exact and wildcard exclusions can be mixed`() {
        val old = Record("Alice", Metadata("2025-01-01", "2025-01-01"), "note1")
        val new = Record("Alice", Metadata("2025-06-01", "2025-06-15"), "note2")

        val result = diff(old, new) {
            exclude("notes", "metadata.*")
        }
        assertFalse(result.hasChanges())
    }

    // --- toPatchMap ---

    @Test
    fun `toPatchMap returns new values for changed fields`() {
        val old = User("Alice", 30, "alice@example.com")
        val new = User("Alice", 31, "alice@new.com")

        val patch = diff(old, new).toPatchMap()
        assertEquals(31, patch["age"])
        assertEquals("alice@new.com", patch["email"])
        assertEquals(2, patch.size)
    }

    @Test
    fun `toPatchMap includes null for removed items`() {
        val changes = listOf(
            Change("items[2]", "old", null, ChangeType.REMOVED)
        )
        val patch = DiffResult(changes).toPatchMap()
        assertTrue(patch.containsKey("items[2]"))
        assertEquals(null, patch["items[2]"])
    }

    @Test
    fun `toPatchMap includes new values for added items`() {
        val changes = listOf(
            Change("items[3]", null, "new", ChangeType.ADDED),
            Change("name", "old", "updated", ChangeType.CHANGED)
        )
        val patch = DiffResult(changes).toPatchMap()
        assertEquals("new", patch["items[3]"])
        assertEquals("updated", patch["name"])
    }

    @Test
    fun `toPatchMap is empty when no changes`() {
        val obj = User("Alice", 30, "alice@example.com")
        val patch = diff(obj, obj.copy()).toPatchMap()
        assertTrue(patch.isEmpty())
    }

    @Test
    fun `ignorePaths excludes exact paths`() {
        data class Config(val host: String, val port: Int, val secret: String)
        val result = diff(
            Config("localhost", 8080, "old-secret"),
            Config("localhost", 9090, "new-secret")
        ) {
            ignorePaths("secret")
        }
        assertEquals(1, result.changes.size)
        assertEquals("port", result.changes[0].path)
    }

    @Test
    fun `humanReadable generates descriptions`() {
        data class Item(val name: String, val price: Int)
        val result = diff(Item("Widget", 10), Item("Widget", 20))
        val readable = result.humanReadable()
        assertEquals(1, readable.size)
        assertTrue(readable[0].contains("changed from"))
        assertTrue(readable[0].contains("10"))
        assertTrue(readable[0].contains("20"))
    }

    @Test
    fun `humanReadable for added items`() {
        data class Container(val items: List<String>)
        val result = diff(Container(listOf("a")), Container(listOf("a", "b")))
        val readable = result.humanReadable()
        assertTrue(readable.any { it.contains("added") })
    }

    @Test
    fun `applyPatch applies changes to map`() {
        val original = mapOf("name" to "Alice" as Any?, "age" to 30 as Any?)
        val changes = DiffResult(listOf(
            Change("age", 30, 31, ChangeType.CHANGED),
            Change("email", null, "alice@test.com", ChangeType.ADDED)
        ))
        val patched = applyPatch(original, changes)
        assertEquals(31, patched["age"])
        assertEquals("alice@test.com", patched["email"])
        assertEquals("Alice", patched["name"])
    }

    @Test
    fun `applyPatch removes entries`() {
        val original = mapOf("name" to "Alice" as Any?, "temp" to "value" as Any?)
        val changes = DiffResult(listOf(
            Change("temp", "value", null, ChangeType.REMOVED)
        ))
        val patched = applyPatch(original, changes)
        assertFalse(patched.containsKey("temp"))
        assertEquals("Alice", patched["name"])
    }
}
