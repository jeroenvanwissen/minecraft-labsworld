package nl.jeroenlabs.labsWorld.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested

/**
 * Comprehensive unit tests for coercion utility functions.
 *
 * Task T1 from .agent/TASKS.md — Tests cover all edge cases, boundary conditions,
 * and error handling for type conversion utilities.
 */
class CoercionsTest {

    @Test
    @DisplayName("Test framework smoke test - should pass")
    fun smokeTest() {
        assertTrue(true, "Basic assertion works")
    }

    // ==================== anyToInt Tests ====================

    @Nested
    @DisplayName("anyToInt()")
    inner class AnyToIntTests {

        @Test
        @DisplayName("should convert valid integers")
        fun validInteger() {
            assertEquals(42, anyToInt(42, 0))
            assertEquals(123, anyToInt("123", 0))
            assertEquals(0, anyToInt("0", -1))
        }

        @Test
        @DisplayName("should handle negative numbers")
        fun negativeNumbers() {
            assertEquals(-42, anyToInt(-42, 0))
            assertEquals(-123, anyToInt("-123", 0))
            assertEquals(-1, anyToInt("-1", 0))
        }

        @Test
        @DisplayName("should convert various number types")
        fun variousNumberTypes() {
            assertEquals(3, anyToInt(3.14, 0))
            assertEquals(42, anyToInt(42.0, 0))
            assertEquals(100, anyToInt(100L, 0))
            assertEquals(5, anyToInt(5.9f, 0))
        }

        @Test
        @DisplayName("should trim whitespace from strings")
        fun trimWhitespace() {
            assertEquals(42, anyToInt("  42  ", 0))
            assertEquals(123, anyToInt("\t123\n", 0))
            assertEquals(-5, anyToInt("  -5  ", 0))
        }

        @Test
        @DisplayName("should return default for invalid input")
        fun invalidInput() {
            assertEquals(0, anyToInt(null, 0))
            assertEquals(10, anyToInt(null, 10))
            assertEquals(5, anyToInt("not a number", 5))
            assertEquals(-1, anyToInt("", -1))
            assertEquals(99, anyToInt("  ", 99))
        }

        @Test
        @DisplayName("should handle boundary values")
        fun boundaryValues() {
            assertEquals(Int.MAX_VALUE, anyToInt(Int.MAX_VALUE, 0))
            assertEquals(Int.MIN_VALUE, anyToInt(Int.MIN_VALUE, 0))
            assertEquals(0, anyToInt("${Long.MAX_VALUE}", 0)) // Overflow returns default
        }

        @Test
        @DisplayName("should reject invalid number formats")
        fun invalidFormats() {
            assertEquals(100, anyToInt("12.34", 100))
            assertEquals(200, anyToInt("1,234", 200))
            assertEquals(300, anyToInt("0x42", 300))
            assertEquals(400, anyToInt("1e5", 400))
        }
    }

    // ==================== anyToString Tests ====================

    @Nested
    @DisplayName("anyToString()")
    inner class AnyToStringTests {

        @Test
        @DisplayName("should convert valid strings")
        fun validStrings() {
            assertEquals("hello", anyToString("hello"))
            assertEquals("world", anyToString("world"))
            assertEquals("test", anyToString("  test  ")) // Trimmed
        }

        @Test
        @DisplayName("should convert numbers to strings")
        fun numberToString() {
            assertEquals("42", anyToString(42))
            assertEquals("3.14", anyToString(3.14))
            assertEquals("-5", anyToString(-5))
            assertEquals("0", anyToString(0))
        }

        @Test
        @DisplayName("should convert booleans to strings")
        fun booleanToString() {
            assertEquals("true", anyToString(true))
            assertEquals("false", anyToString(false))
        }

        @Test
        @DisplayName("should return null for null and empty inputs")
        fun nullAndEmpty() {
            assertNull(anyToString(null))
            assertNull(anyToString(""))
            assertNull(anyToString("   "))
            assertNull(anyToString("\t\n"))
        }

        @Test
        @DisplayName("should handle special characters")
        fun specialCharacters() {
            assertEquals("hello world!", anyToString("hello world!"))
            assertEquals("test@example.com", anyToString("test@example.com"))
            assertEquals("path/to/file", anyToString("path/to/file"))
            assertEquals("emoji 🎉", anyToString("emoji 🎉"))
        }

        @Test
        @DisplayName("should convert objects via toString()")
        fun objectToString() {
            val list = listOf(1, 2, 3)
            assertEquals("[1, 2, 3]", anyToString(list))
        }
    }

    // ==================== anyToBool Tests ====================

    @Nested
    @DisplayName("anyToBool()")
    inner class AnyToBoolTests {

        @Test
        @DisplayName("should parse true values")
        fun trueValues() {
            assertEquals(true, anyToBool(true))
            assertEquals(true, anyToBool("true"))
            assertEquals(true, anyToBool("yes"))
            assertEquals(true, anyToBool("1"))
            assertEquals(true, anyToBool(1))
        }

        @Test
        @DisplayName("should parse false values")
        fun falseValues() {
            assertEquals(false, anyToBool(false))
            assertEquals(false, anyToBool("false"))
            assertEquals(false, anyToBool("no"))
            assertEquals(false, anyToBool("0"))
            assertEquals(false, anyToBool(0))
        }

        @Test
        @DisplayName("should be case insensitive")
        fun caseInsensitive() {
            assertEquals(true, anyToBool("TRUE"))
            assertEquals(true, anyToBool("True"))
            assertEquals(true, anyToBool("YES"))
            assertEquals(true, anyToBool("Yes"))
            assertEquals(false, anyToBool("FALSE"))
            assertEquals(false, anyToBool("False"))
            assertEquals(false, anyToBool("NO"))
            assertEquals(false, anyToBool("No"))
        }

        @Test
        @DisplayName("should handle whitespace")
        fun whitespace() {
            assertEquals(true, anyToBool("  true  "))
            assertEquals(false, anyToBool("  false  "))
            assertEquals(true, anyToBool("\tyes\n"))
            assertEquals(false, anyToBool("\tno\n"))
        }

        @Test
        @DisplayName("should handle various number types")
        fun variousNumbers() {
            assertEquals(true, anyToBool(1))
            assertEquals(true, anyToBool(42))
            assertEquals(true, anyToBool(-1))
            assertEquals(false, anyToBool(0))
            assertEquals(true, anyToBool(1.0))
            assertEquals(false, anyToBool(0.0))
        }

        @Test
        @DisplayName("should return default for null and invalid input")
        fun nullAndDefaults() {
            assertNull(anyToBool(null))
            assertEquals(true, anyToBool(null, true))
            assertEquals(false, anyToBool(null, false))
            assertEquals(true, anyToBool("invalid", true))
            assertEquals(false, anyToBool("maybe", false))
            assertNull(anyToBool("unknown"))
        }
    }

    // ==================== anyToDouble Tests ====================

    @Nested
    @DisplayName("anyToDouble()")
    inner class AnyToDoubleTests {

        @Test
        @DisplayName("should convert valid doubles")
        fun validDoubles() {
            assertEquals(3.14, anyToDouble(3.14, 0.0), 0.001)
            assertEquals(42.0, anyToDouble(42, 0.0), 0.001)
            assertEquals(123.45, anyToDouble("123.45", 0.0), 0.001)
        }

        @Test
        @DisplayName("should handle negative numbers")
        fun negativeNumbers() {
            assertEquals(-3.14, anyToDouble(-3.14, 0.0), 0.001)
            assertEquals(-42.0, anyToDouble("-42", 0.0), 0.001)
            assertEquals(-0.5, anyToDouble("-0.5", 0.0), 0.001)
        }

        @Test
        @DisplayName("should convert various number types")
        fun variousNumberTypes() {
            assertEquals(42.0, anyToDouble(42, 0.0), 0.001)
            assertEquals(100.0, anyToDouble(100L, 0.0), 0.001)
            assertEquals(5.5, anyToDouble(5.5f, 0.01), 0.01)
        }

        @Test
        @DisplayName("should trim whitespace from strings")
        fun trimWhitespace() {
            assertEquals(3.14, anyToDouble("  3.14  ", 0.0), 0.001)
            assertEquals(42.0, anyToDouble("\t42.0\n", 0.0), 0.001)
        }

        @Test
        @DisplayName("should return default for invalid input")
        fun invalidInput() {
            assertEquals(0.0, anyToDouble(null, 0.0), 0.001)
            assertEquals(1.5, anyToDouble(null, 1.5), 0.001)
            assertEquals(2.5, anyToDouble("invalid", 2.5), 0.001)
            assertEquals(3.0, anyToDouble("", 3.0), 0.001)
            assertEquals(4.0, anyToDouble("  ", 4.0), 0.001)
        }

        @Test
        @DisplayName("should handle boundary values")
        fun boundaryValues() {
            assertEquals(0.0, anyToDouble(0.0, -1.0), 0.001)
            assertEquals(Double.MAX_VALUE, anyToDouble(Double.MAX_VALUE, 0.0), 0.001)
            assertEquals(Double.MIN_VALUE, anyToDouble(Double.MIN_VALUE, 0.0), 0.001)
        }

        @Test
        @DisplayName("should handle special double values")
        fun specialValues() {
            assertEquals(0.0001, anyToDouble("0.0001", 0.0), 0.00001)
            assertEquals(1000000.0, anyToDouble("1000000", 0.0), 0.001)
            assertEquals(0.1, anyToDouble(".1", 0.0), 0.001)
        }

        @Test
        @DisplayName("should reject invalid formats")
        fun invalidFormats() {
            assertEquals(100.0, anyToDouble("1,234.56", 100.0), 0.001)
            assertEquals(200.0, anyToDouble("not a number", 200.0), 0.001)
        }
    }

    // ==================== anyToStringList Tests ====================

    @Nested
    @DisplayName("anyToStringList()")
    inner class AnyToStringListTests {

        @Test
        @DisplayName("should convert lists directly")
        fun convertLists() {
            assertEquals(listOf("a", "b", "c"), anyToStringList(listOf("a", "b", "c")))
            assertEquals(listOf("1", "2", "3"), anyToStringList(listOf(1, 2, 3)))
            assertEquals(listOf("true", "false"), anyToStringList(listOf(true, false)))
        }

        @Test
        @DisplayName("should parse CSV strings")
        fun parseCSV() {
            assertEquals(listOf("x", "y", "z"), anyToStringList("x,y,z"))
            assertEquals(listOf("item1", "item2"), anyToStringList("item1, item2"))
            assertEquals(listOf("a", "b", "c"), anyToStringList("a,b,c"))
        }

        @Test
        @DisplayName("should handle single values")
        fun singleValue() {
            assertEquals(listOf("single"), anyToStringList("single"))
            assertEquals(listOf("one-item"), anyToStringList("one-item"))
        }

        @Test
        @DisplayName("should return empty list for null and empty inputs")
        fun nullAndEmpty() {
            assertTrue(anyToStringList(null).isEmpty())
            assertTrue(anyToStringList("").isEmpty())
            assertTrue(anyToStringList("   ").isEmpty())
        }

        @Test
        @DisplayName("should filter out empty values in CSV")
        fun filterEmptyInCSV() {
            assertEquals(listOf("a", "b"), anyToStringList("a,,b,"))
            assertEquals(listOf("x", "y", "z"), anyToStringList("x, , y, , z"))
            assertEquals(listOf("item"), anyToStringList(",item,"))
        }

        @Test
        @DisplayName("should trim whitespace from list items")
        fun trimItems() {
            assertEquals(listOf("a", "b", "c"), anyToStringList(" a , b , c "))
            assertEquals(listOf("x", "y"), anyToStringList("  x  ,  y  "))
        }

        @Test
        @DisplayName("should handle special characters")
        fun specialCharacters() {
            assertEquals(listOf("test@example.com", "user@domain.com"),
                anyToStringList("test@example.com,user@domain.com"))
            assertEquals(listOf("path/to/file", "another/path"),
                anyToStringList("path/to/file,another/path"))
        }

        @Test
        @DisplayName("should handle lists with null elements")
        fun listsWithNulls() {
            assertEquals(listOf("a", "c"), anyToStringList(listOf("a", null, "c")))
            assertEquals(listOf("x"), anyToStringList(listOf(null, "x", null)))
        }

        @Test
        @DisplayName("should handle lists with empty strings")
        fun listsWithEmptyStrings() {
            assertEquals(listOf("a", "b"), anyToStringList(listOf("a", "", "b")))
            assertEquals(listOf("x"), anyToStringList(listOf("", "x", "  ")))
        }

        @Test
        @DisplayName("should return empty list for non-list, non-string types")
        fun nonListNonString() {
            assertTrue(anyToStringList(42).isEmpty())
            assertTrue(anyToStringList(true).isEmpty())
            assertTrue(anyToStringList(3.14).isEmpty())
        }
    }
}
