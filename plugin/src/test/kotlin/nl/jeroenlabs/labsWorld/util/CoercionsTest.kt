package nl.jeroenlabs.labsWorld.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

/**
 * Smoke test for the test framework.
 * Validates that JUnit 5 is configured correctly and basic coercion functions work.
 */
class CoercionsTest {

    @Test
    @DisplayName("Test framework smoke test - should pass")
    fun smokeTest() {
        assertTrue(true, "Basic assertion works")
    }

    @Test
    @DisplayName("anyToInt should convert valid integers")
    fun testAnyToInt_validInteger() {
        assertEquals(42, anyToInt(42, 0))
        assertEquals(123, anyToInt("123", 0))
        assertEquals(0, anyToInt("0", -1))
    }

    @Test
    @DisplayName("anyToInt should return default for invalid input")
    fun testAnyToInt_invalidInput() {
        assertEquals(0, anyToInt(null, 0))
        assertEquals(10, anyToInt(null, 10))
        assertEquals(5, anyToInt("not a number", 5))
        assertEquals(-1, anyToInt("", -1))
    }

    @Test
    @DisplayName("anyToString should convert values to strings")
    fun testAnyToString() {
        assertEquals("hello", anyToString("hello"))
        assertEquals("42", anyToString(42))
        assertEquals("true", anyToString(true))
        assertNull(anyToString(null))
        assertNull(anyToString(""))
        assertNull(anyToString("   "))
    }

    @Test
    @DisplayName("anyToBool should parse boolean values")
    fun testAnyToBool() {
        // True values
        assertEquals(true, anyToBool(true))
        assertEquals(true, anyToBool("true"))
        assertEquals(true, anyToBool("yes"))
        assertEquals(true, anyToBool("1"))
        assertEquals(true, anyToBool(1))

        // False values
        assertEquals(false, anyToBool(false))
        assertEquals(false, anyToBool("false"))
        assertEquals(false, anyToBool("no"))
        assertEquals(false, anyToBool("0"))
        assertEquals(false, anyToBool(0))

        // Null and defaults
        assertNull(anyToBool(null))
        assertEquals(true, anyToBool(null, true))
        assertEquals(false, anyToBool(null, false))
        assertEquals(true, anyToBool("invalid", true))
    }

    @Test
    @DisplayName("anyToDouble should convert to doubles")
    fun testAnyToDouble() {
        assertEquals(3.14, anyToDouble(3.14, 0.0), 0.001)
        assertEquals(42.0, anyToDouble(42, 0.0), 0.001)
        assertEquals(123.45, anyToDouble("123.45", 0.0), 0.001)
        assertEquals(0.0, anyToDouble(null, 0.0), 0.001)
        assertEquals(1.5, anyToDouble(null, 1.5), 0.001)
        assertEquals(2.5, anyToDouble("invalid", 2.5), 0.001)
    }

    @Test
    @DisplayName("anyToStringList should parse lists and CSV strings")
    fun testAnyToStringList() {
        // List input
        assertEquals(listOf("a", "b", "c"), anyToStringList(listOf("a", "b", "c")))

        // CSV string input
        assertEquals(listOf("x", "y", "z"), anyToStringList("x,y,z"))
        assertEquals(listOf("item1", "item2"), anyToStringList("item1, item2"))

        // Single value
        assertEquals(listOf("single"), anyToStringList("single"))

        // Empty/null
        assertTrue(anyToStringList(null).isEmpty())
        assertTrue(anyToStringList("").isEmpty())

        // Mixed with empty values
        assertEquals(listOf("a", "b"), anyToStringList("a,,b,"))
    }
}
