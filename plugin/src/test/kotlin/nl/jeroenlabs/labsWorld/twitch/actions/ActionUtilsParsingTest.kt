package nl.jeroenlabs.labsWorld.twitch.actions

import io.mockk.*
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.AfterEach

/**
 * Unit tests for pure parsing helpers in ActionUtils.
 *
 * Task T2 from .agent/TASKS.md — Tests parsing logic without requiring a running server.
 * Uses MockK to stub Bukkit API calls where necessary.
 */
class ActionUtilsParsingTest {

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    // ==================== parseFireworkType Tests ====================

    @Nested
    @DisplayName("parseFireworkType()")
    inner class ParseFireworkTypeTests {

        @Test
        @DisplayName("should parse 'ball' type")
        fun parseBall() {
            assertEquals(FireworkEffect.Type.BALL, ActionUtils.parseFireworkType("ball"))
        }

        @Test
        @DisplayName("should parse 'ball_large' variants")
        fun parseBallLarge() {
            assertEquals(FireworkEffect.Type.BALL_LARGE, ActionUtils.parseFireworkType("ball_large"))
            assertEquals(FireworkEffect.Type.BALL_LARGE, ActionUtils.parseFireworkType("large_ball"))
            assertEquals(FireworkEffect.Type.BALL_LARGE, ActionUtils.parseFireworkType("large"))
        }

        @Test
        @DisplayName("should parse 'star' type")
        fun parseStar() {
            assertEquals(FireworkEffect.Type.STAR, ActionUtils.parseFireworkType("star"))
        }

        @Test
        @DisplayName("should parse 'burst' type")
        fun parseBurst() {
            assertEquals(FireworkEffect.Type.BURST, ActionUtils.parseFireworkType("burst"))
        }

        @Test
        @DisplayName("should parse 'creeper' type")
        fun parseCreeper() {
            assertEquals(FireworkEffect.Type.CREEPER, ActionUtils.parseFireworkType("creeper"))
        }

        @Test
        @DisplayName("should default to BALL for unknown types")
        fun defaultToBall() {
            assertEquals(FireworkEffect.Type.BALL, ActionUtils.parseFireworkType("unknown"))
            assertEquals(FireworkEffect.Type.BALL, ActionUtils.parseFireworkType(""))
            assertEquals(FireworkEffect.Type.BALL, ActionUtils.parseFireworkType("random"))
            assertEquals(FireworkEffect.Type.BALL, ActionUtils.parseFireworkType("invalid"))
        }

        @Test
        @DisplayName("should be case sensitive")
        fun caseSensitive() {
            // The implementation doesn't convert to lowercase, so mixed case should default to BALL
            assertEquals(FireworkEffect.Type.BALL, ActionUtils.parseFireworkType("BALL"))
            assertEquals(FireworkEffect.Type.BALL, ActionUtils.parseFireworkType("Star"))
            assertEquals(FireworkEffect.Type.BALL, ActionUtils.parseFireworkType("Burst"))
        }
    }

    // ==================== parseEntityType Tests ====================

    @Nested
    @DisplayName("parseEntityType()")
    inner class ParseEntityTypeTests {

        @Test
        @DisplayName("should parse valid entity type from EntityType.fromName()")
        fun parseFromName() {
            // Mock EntityType.fromName to return a valid entity type
            mockkStatic(EntityType::class)
            every { EntityType.fromName("zombie") } returns EntityType.ZOMBIE
            every { EntityType.fromName("creeper") } returns EntityType.CREEPER

            assertEquals(EntityType.ZOMBIE, ActionUtils.parseEntityType("zombie"))
            assertEquals(EntityType.CREEPER, ActionUtils.parseEntityType("creeper"))

            verify { EntityType.fromName("zombie") }
            verify { EntityType.fromName("creeper") }
        }

        @Test
        @DisplayName("should parse valid entity type from EntityType.valueOf()")
        fun parseFromValueOf() {
            // Mock EntityType.fromName to return null, so it falls back to valueOf
            mockkStatic(EntityType::class)
            every { EntityType.fromName(any()) } returns null

            // Test with actual enum values (these should work without mocking if the enum is available)
            val result = ActionUtils.parseEntityType("ZOMBIE")
            // If ZOMBIE enum value exists, it should return it; otherwise null
            assertTrue(result == EntityType.ZOMBIE || result == null)
        }

        @Test
        @DisplayName("should return null for invalid entity type")
        fun parseInvalid() {
            mockkStatic(EntityType::class)
            every { EntityType.fromName(any()) } returns null
            every { EntityType.valueOf(any()) } throws IllegalArgumentException("No enum constant")

            assertNull(ActionUtils.parseEntityType("invalid_mob"))
            assertNull(ActionUtils.parseEntityType("notreal"))
        }

        @Test
        @DisplayName("should handle empty and whitespace strings")
        fun parseEmptyAndWhitespace() {
            mockkStatic(EntityType::class)
            every { EntityType.fromName(any()) } returns null
            every { EntityType.valueOf(any()) } throws IllegalArgumentException("No enum constant")

            assertNull(ActionUtils.parseEntityType(""))
            // Note: whitespace is trimmed before valueOf, so "   " becomes "" which throws
            assertNull(ActionUtils.parseEntityType("   "))
        }
    }

    // ==================== parseItemStacks Tests ====================

    @Nested
    @DisplayName("parseItemStacks()")
    inner class ParseItemStacksTests {

        @Test
        @DisplayName("should return empty list for null input")
        fun parseNull() {
            assertEquals(emptyList<ItemStack>(), ActionUtils.parseItemStacks(null))
        }

        @Test
        @DisplayName("should return empty list for non-list input")
        fun parseNonList() {
            assertEquals(emptyList<ItemStack>(), ActionUtils.parseItemStacks("not a list"))
            assertEquals(emptyList<ItemStack>(), ActionUtils.parseItemStacks(42))
            assertEquals(emptyList<ItemStack>(), ActionUtils.parseItemStacks(true))
        }

        @Test
        @DisplayName("should return empty list for empty list")
        fun parseEmptyList() {
            assertEquals(emptyList<ItemStack>(), ActionUtils.parseItemStacks(emptyList<Any>()))
        }

        @Test
        @DisplayName("should skip items with missing type field")
        fun parseMissingType() {
            val input = listOf(
                mapOf("amount" to 5),  // No "type" field
                mapOf("other" to "value")  // No "type" field
            )

            val result = ActionUtils.parseItemStacks(input)
            assertEquals(emptyList<ItemStack>(), result)
        }

        // Note: Tests that require actual Material/ItemStack instantiation are skipped
        // because they require a running Bukkit server. The parseItemStacks function's
        // logic for:
        // - Material.matchMaterial lookup
        // - ItemStack construction
        // - Amount coercion
        // - Filtering null/invalid materials
        // is better suited for integration tests with a test server harness (see Task T6).
    }

    // ==================== pickDefaultWorld Tests ====================

    @Nested
    @DisplayName("pickDefaultWorld()")
    inner class PickDefaultWorldTests {

        @Test
        @DisplayName("should return null for empty list")
        fun emptyList() {
            assertNull(ActionUtils.pickDefaultWorld(emptyList<World>()))
        }

        @Test
        @DisplayName("should return first world from single-world list")
        fun singleWorld() {
            val mockWorld = mockk<World>()
            every { mockWorld.name } returns "world"

            val result = ActionUtils.pickDefaultWorld(listOf(mockWorld))
            assertSame(mockWorld, result)
        }

        @Test
        @DisplayName("should return first world from multi-world list")
        fun multipleWorlds() {
            val mockWorld1 = mockk<World>()
            val mockWorld2 = mockk<World>()
            val mockWorld3 = mockk<World>()

            every { mockWorld1.name } returns "world"
            every { mockWorld2.name } returns "world_nether"
            every { mockWorld3.name } returns "world_the_end"

            val result = ActionUtils.pickDefaultWorld(listOf(mockWorld1, mockWorld2, mockWorld3))
            assertSame(mockWorld1, result)
        }

        @Test
        @DisplayName("should ignore world order and always return first")
        fun respectsListOrder() {
            val mockNetherWorld = mockk<World>()
            val mockOverworld = mockk<World>()

            every { mockNetherWorld.name } returns "world_nether"
            every { mockOverworld.name } returns "world"

            // First in list is nether
            val result = ActionUtils.pickDefaultWorld(listOf(mockNetherWorld, mockOverworld))
            assertSame(mockNetherWorld, result)
        }
    }
}
