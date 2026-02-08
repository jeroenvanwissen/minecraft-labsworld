package nl.jeroenlabs.labsWorld.twitch.actions.handlers

import io.mockk.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.scheduler.BukkitScheduler
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.TwitchChat
import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import nl.jeroenlabs.labsWorld.twitch.actions.ActionUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.logging.Logger

/**
 * Unit tests for LootChestActionHandler.
 *
 * Limitation: Material.isSolid and Material.isAir require Bukkit's registry
 * system, which is unavailable in unit tests. Tests use mock Material instances
 * for block type checks in findChestSpawnLocation. The Material.HOPPER equality
 * check cannot be tested directly because isSolid is called first on the real
 * enum value, triggering ExceptionInInitializerError.
 */
class LootChestActionHandlerTest {

    private lateinit var plugin: LabsWorld
    private lateinit var twitchClient: TwitchClient
    private lateinit var twitchChat: TwitchChat
    private lateinit var twitchConfigManager: TwitchConfigManager
    private lateinit var context: TwitchContext
    private lateinit var scheduler: BukkitScheduler
    private lateinit var logger: Logger
    private lateinit var player: Player
    private lateinit var world: World
    private lateinit var playerLocation: Location
    private lateinit var handler: LootChestActionHandler

    private val defaultInvocation = ActionInvocation(
        userId = "user123",
        userName = "testuser",
        channelName = "testchannel",
        message = null,
        rewardId = null,
        rewardTitle = null,
        userInput = null,
        commandName = null,
    )

    @BeforeEach
    fun setup() {
        plugin = mockk(relaxed = true)
        twitchClient = mockk(relaxed = true)
        twitchChat = mockk(relaxed = true)
        twitchConfigManager = mockk(relaxed = true)
        scheduler = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        player = mockk(relaxed = true)
        world = mockk(relaxed = true)
        playerLocation = mockk(relaxed = true)

        every { plugin.server.scheduler } returns scheduler
        every { plugin.logger } returns logger
        every { twitchClient.chat } returns twitchChat
        every { player.location } returns playerLocation
        every { player.name } returns "testuser"
        every { playerLocation.world } returns world
        every { playerLocation.blockX } returns 100
        every { playerLocation.blockZ } returns 200

        // Execute scheduler tasks synchronously
        every { scheduler.runTask(plugin, any<Runnable>()) } answers {
            secondArg<Runnable>().run()
            mockk(relaxed = true)
        }

        context = TwitchContext(
            plugin = plugin,
            twitchClient = twitchClient,
            twitchConfigManager = twitchConfigManager,
        )

        mockkObject(ActionUtils)

        handler = LootChestActionHandler()
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    /**
     * Creates a mock Material with the given isSolid/isAir properties.
     * Real Material enum values cannot be used because isSolid/isAir
     * require Bukkit's registry system to be initialized.
     */
    private fun mockMaterial(solid: Boolean = false, air: Boolean = false): Material {
        val mat = mockk<Material>()
        every { mat.isSolid } returns solid
        every { mat.isAir } returns air
        return mat
    }

    /**
     * Sets up a mock world where findChestSpawnLocation will succeed on the first try.
     * Returns the place block and chest state for verification.
     */
    private fun mockSuccessfulChestPlacement(): Triple<Block, Block, Chest> {
        val floorBlock = mockk<Block>(relaxed = true)
        val placeBlock = mockk<Block>(relaxed = true)
        val chestState = mockk<Chest>(relaxed = true)
        val inventory = mockk<Inventory>(relaxed = true)
        val placeLocation = mockk<Location>(relaxed = true)

        every { world.maxHeight } returns 256
        every { world.getHighestBlockYAt(any<Int>(), any<Int>()) } returns 64
        every { world.getBlockAt(any<Int>(), eq(64), any<Int>()) } returns floorBlock
        every { world.getBlockAt(any<Int>(), eq(65), any<Int>()) } returns placeBlock

        // Use mock Materials to avoid registry initialization
        every { floorBlock.type } returns mockMaterial(solid = true)
        every { placeBlock.type } returns mockMaterial(air = true)
        every { placeBlock.location } returns placeLocation
        every { placeBlock.state } returns chestState
        every { placeBlock.x } returns 100
        every { placeBlock.y } returns 65
        every { placeBlock.z } returns 200
        // Close the loop: handler calls spawnLocation.block after findChestSpawnLocation
        every { placeLocation.block } returns placeBlock

        every { chestState.blockInventory } returns inventory
        every { inventory.size } returns 27
        every { inventory.contents } returns arrayOfNulls(27)

        return Triple(floorBlock, placeBlock, chestState)
    }

    // ==================== Handle Basic Flow ====================

    @Nested
    @DisplayName("Handle Basic Flow")
    inner class HandleBasicFlowTests {

        @Test
        @DisplayName("should return early when no target player found")
        fun returnsEarlyWhenNoTargetPlayer() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns null

            assertDoesNotThrow {
                handler.handle(context, defaultInvocation, emptyMap())
            }

            verify(exactly = 0) { scheduler.runTask(any(), any<Runnable>()) }
        }

        @Test
        @DisplayName("should error when no loot defined and no debug bread")
        fun errorsWhenNoLootDefined() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, emptyMap())
            }

            assertEquals("No loot defined", exception.message)
        }

        @Test
        @DisplayName("should error when loot list is empty")
        fun errorsWhenEmptyLootList() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player

            val params = mapOf<String, Any?>("loot" to emptyList<Any>())

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("No loot defined", exception.message)
        }

        @Test
        @DisplayName("should error when no safe chest location found")
        fun errorsWhenNoSafeLocation() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            every { world.maxHeight } returns 256
            every { world.getHighestBlockYAt(any<Int>(), any<Int>()) } returns 64
            val nonSolidBlock = mockk<Block>(relaxed = true)
            every { nonSolidBlock.type } returns mockMaterial(solid = false)
            every { world.getBlockAt(any<Int>(), eq(64), any<Int>()) } returns nonSolidBlock

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0))
            val params = mapOf<String, Any?>("loot" to loot)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("Could not find a safe spot to place a chest", exception.message)
        }

        @Test
        @DisplayName("should place chest and schedule fill runner on happy path")
        fun placesChestOnHappyPath() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            val (_, placeBlock, chestState) = mockSuccessfulChestPlacement()

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot)

            try {
                handler.handle(context, defaultInvocation, params)
            } catch (_: Throwable) {
                // ItemStack constructor may fail without server
            }

            verify { placeBlock.type = Material.CHEST }
            verify { chestState.update(true, false) }
        }
    }

    // ==================== Loot Parsing ====================

    @Nested
    @DisplayName("Loot Parsing")
    inner class LootParsingTests {

        @Test
        @DisplayName("should ignore entries with missing type")
        fun ignoresEntriesWithMissingType() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player

            val loot = listOf(
                mapOf("amount" to 5, "weight" to 1.0),
                mapOf("other" to "value"),
            )
            val params = mapOf<String, Any?>("loot" to loot)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("No loot defined", exception.message)
        }

        @Test
        @DisplayName("should ignore entries with zero weight")
        fun ignoresEntriesWithZeroWeight() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            val loot = listOf(
                mapOf("type" to "DIAMOND", "weight" to 0.0),
            )
            val params = mapOf<String, Any?>("loot" to loot)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("No loot defined", exception.message)
        }

        @Test
        @DisplayName("should ignore entries with negative weight")
        fun ignoresEntriesWithNegativeWeight() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("GOLD_INGOT") } returns Material.GOLD_INGOT

            val loot = listOf(
                mapOf("type" to "GOLD_INGOT", "weight" to -5.0),
            )
            val params = mapOf<String, Any?>("loot" to loot)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("No loot defined", exception.message)
        }

        @Test
        @DisplayName("should ignore entries with unrecognized material")
        fun ignoresUnrecognizedMaterial() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("FAKE_ITEM") } returns null

            val loot = listOf(
                mapOf("type" to "FAKE_ITEM", "weight" to 1.0),
            )
            val params = mapOf<String, Any?>("loot" to loot)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("No loot defined", exception.message)
        }

        @Test
        @DisplayName("should accept 'material' key as alternative to 'type'")
        fun acceptsMaterialKey() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("IRON_INGOT") } returns Material.IRON_INGOT

            val (_, placeBlock, _) = mockSuccessfulChestPlacement()

            val loot = listOf(
                mapOf("material" to "IRON_INGOT", "weight" to 1.0, "amount" to 1),
            )
            val params = mapOf<String, Any?>("loot" to loot)

            try {
                handler.handle(context, defaultInvocation, params)
            } catch (_: Throwable) {
                // ItemStack constructor may fail without server
            }

            // Passed parseLootEntries without "No loot defined" error
            verify { Material.matchMaterial("IRON_INGOT") }
            verify { placeBlock.type = Material.CHEST }
        }

        @Test
        @DisplayName("should accept 'items' key as alternative to 'loot'")
        fun acceptsItemsKey() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            val (_, placeBlock, _) = mockSuccessfulChestPlacement()

            val loot = listOf(
                mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1),
            )
            val params = mapOf<String, Any?>("items" to loot)

            try {
                handler.handle(context, defaultInvocation, params)
            } catch (_: Throwable) {
                // ItemStack constructor may fail without server
            }

            verify { Material.matchMaterial("DIAMOND") }
            verify { placeBlock.type = Material.CHEST }
        }

        @Test
        @DisplayName("should accept 'loot_table' key as alternative to 'loot'")
        fun acceptsLootTableKey() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("EMERALD") } returns Material.EMERALD

            val (_, placeBlock, _) = mockSuccessfulChestPlacement()

            val loot = listOf(
                mapOf("type" to "EMERALD", "weight" to 1.0, "amount" to 1),
            )
            val params = mapOf<String, Any?>("loot_table" to loot)

            try {
                handler.handle(context, defaultInvocation, params)
            } catch (_: Throwable) {
                // ItemStack constructor may fail without server
            }

            verify { Material.matchMaterial("EMERALD") }
            verify { placeBlock.type = Material.CHEST }
        }
    }

    // ==================== Chest Placement ====================

    @Nested
    @DisplayName("Chest Placement")
    inner class ChestPlacementTests {

        @Test
        @DisplayName("should place chest on solid ground")
        fun placesChestOnSolidGround() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            val (_, placeBlock, _) = mockSuccessfulChestPlacement()

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot)

            try {
                handler.handle(context, defaultInvocation, params)
            } catch (_: Throwable) {
                // ItemStack constructor may fail
            }

            verify { placeBlock.type = Material.CHEST }
        }

        @Test
        @DisplayName("should fail when all floors are non-solid")
        fun failsWhenAllFloorsNonSolid() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            every { world.maxHeight } returns 256
            every { world.getHighestBlockYAt(any<Int>(), any<Int>()) } returns 64

            val nonSolidBlock = mockk<Block>(relaxed = true)
            every { nonSolidBlock.type } returns mockMaterial(solid = false)
            every { world.getBlockAt(any<Int>(), eq(64), any<Int>()) } returns nonSolidBlock

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot, "tries" to 5)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("Could not find a safe spot to place a chest", exception.message)
        }

        @Test
        @DisplayName("should skip non-air placement locations")
        fun skipsNonAirPlacementLocations() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            every { world.maxHeight } returns 256
            every { world.getHighestBlockYAt(any<Int>(), any<Int>()) } returns 64

            val floorBlock = mockk<Block>(relaxed = true)
            every { floorBlock.type } returns mockMaterial(solid = true)

            val occupiedBlock = mockk<Block>(relaxed = true)
            every { occupiedBlock.type } returns mockMaterial(solid = true, air = false)

            every { world.getBlockAt(any<Int>(), eq(64), any<Int>()) } returns floorBlock
            every { world.getBlockAt(any<Int>(), eq(65), any<Int>()) } returns occupiedBlock

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot, "tries" to 5)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("Could not find a safe spot to place a chest", exception.message)
        }

        @Test
        @DisplayName("should skip locations above max height")
        fun skipsLocationsAboveMaxHeight() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            every { world.maxHeight } returns 256
            every { world.getHighestBlockYAt(any<Int>(), any<Int>()) } returns 255

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot, "tries" to 5)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("Could not find a safe spot to place a chest", exception.message)
        }

        @Test
        @DisplayName("should fail when world is null")
        fun failsWhenWorldIsNull() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND
            every { playerLocation.world } returns null

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("Could not find a safe spot to place a chest", exception.message)
        }
    }

    // ==================== Debug Bread ====================

    @Nested
    @DisplayName("Debug Bread")
    inner class DebugBreadTests {

        @Test
        @DisplayName("should proceed without error when debug_force_bread_amount is set and no loot")
        fun debugBreadWorksWithoutLoot() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player

            val (_, placeBlock, _) = mockSuccessfulChestPlacement()

            val params = mapOf<String, Any?>("debug_force_bread_amount" to 5)

            try {
                handler.handle(context, defaultInvocation, params)
            } catch (_: Throwable) {
                // ItemStack constructor may fail without server
            }

            verify { placeBlock.type = Material.CHEST }
        }

        @Test
        @DisplayName("should still error when debug_force_bread_amount is zero with no loot")
        fun debugBreadZeroStillErrors() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player

            val params = mapOf<String, Any?>("debug_force_bread_amount" to 0)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("No loot defined", exception.message)
        }

        @Test
        @DisplayName("should still error when debug_force_bread_amount is negative with no loot")
        fun debugBreadNegativeStillErrors() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player

            val params = mapOf<String, Any?>("debug_force_bread_amount" to -1)

            val exception = assertThrows(IllegalStateException::class.java) {
                handler.handle(context, defaultInvocation, params)
            }

            assertEquals("No loot defined", exception.message)
        }

        @Test
        @DisplayName("should enter fill runner when debug_force_bread_amount is set")
        fun entersFillRunnerWithDebugBread() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player

            val (_, _, chestState) = mockSuccessfulChestPlacement()
            val inventory = mockk<Inventory>(relaxed = true)
            every { chestState.blockInventory } returns inventory
            every { inventory.size } returns 27
            every { inventory.contents } returns arrayOfNulls(27)

            val params = mapOf<String, Any?>("debug_force_bread_amount" to 3)

            try {
                handler.handle(context, defaultInvocation, params)
            } catch (_: Throwable) {
                // ItemStack constructor requires server — expected
            }

            // Verify inventory was cleared (happens before ItemStack creation)
            verify { inventory.clear() }
        }
    }

    // ==================== Announcement ====================

    @Nested
    @DisplayName("Announcement")
    inner class AnnouncementTests {

        /**
         * Announcement tests skip the fill runner because ItemStack constructors
         * require a running server. The fill runner is scheduled but not executed,
         * so the announcement code (which runs after scheduling) is reached.
         */
        @BeforeEach
        fun skipFillRunner() {
            // Override default: don't execute the fill runner
            every { scheduler.runTask(plugin, any<Runnable>()) } returns mockk(relaxed = true)
        }

        @Test
        @DisplayName("should announce chest location when announce=true")
        fun announcesWhenTrue() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            mockSuccessfulChestPlacement()

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot, "announce" to true)

            handler.handle(context, defaultInvocation, params)

            verify {
                twitchChat.sendMessage(
                    eq("testchannel"),
                    match<String> { it.contains("testuser") && it.contains("Loot Box") },
                )
            }
        }

        @Test
        @DisplayName("should announce by default when announce param not specified")
        fun announcesByDefault() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            mockSuccessfulChestPlacement()

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot)

            handler.handle(context, defaultInvocation, params)

            verify {
                twitchChat.sendMessage(eq("testchannel"), any<String>())
            }
        }

        @Test
        @DisplayName("should not announce when announce=false")
        fun doesNotAnnounceWhenFalse() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            mockSuccessfulChestPlacement()

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot, "announce" to false)

            handler.handle(context, defaultInvocation, params)

            verify(exactly = 0) {
                twitchChat.sendMessage(any(), any<String>())
            }
        }

        @Test
        @DisplayName("should include custom chest name in announcement")
        fun includesCustomNameInAnnouncement() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            mockSuccessfulChestPlacement()

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>(
                "loot" to loot,
                "announce" to true,
                "name" to "Epic Treasure",
            )

            handler.handle(context, defaultInvocation, params)

            verify {
                twitchChat.sendMessage(
                    eq("testchannel"),
                    match<String> { it.contains("Epic Treasure") },
                )
            }
        }
    }

    // ==================== Parameter Defaults ====================

    @Nested
    @DisplayName("Parameter Defaults")
    inner class ParameterDefaultsTests {

        @BeforeEach
        fun skipFillRunner() {
            every { scheduler.runTask(plugin, any<Runnable>()) } returns mockk(relaxed = true)
        }

        @Test
        @DisplayName("should use default chest name 'Loot Box' when name param not specified")
        fun defaultChestName() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            mockSuccessfulChestPlacement()

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot)

            handler.handle(context, defaultInvocation, params)

            verify {
                twitchChat.sendMessage(
                    any(),
                    match<String> { it.contains("Loot Box") },
                )
            }
        }

        @Test
        @DisplayName("should use 'Loot Box' when name param is blank")
        fun defaultChestNameWhenBlank() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            mockSuccessfulChestPlacement()

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot, "name" to "  ")

            handler.handle(context, defaultInvocation, params)

            verify {
                twitchChat.sendMessage(
                    any(),
                    match<String> { it.contains("Loot Box") },
                )
            }
        }
    }

    // ==================== Additional Types ====================

    @Nested
    @DisplayName("Additional Types")
    inner class AdditionalTypesTests {

        @Test
        @DisplayName("should have type 'world.loot_chest'")
        fun hasCorrectType() {
            assertEquals("world.loot_chest", handler.type)
        }

        @Test
        @DisplayName("should have 'player.loot_chest' in additionalTypes")
        fun hasPlayerLootChestType() {
            assertTrue(handler.additionalTypes.contains("player.loot_chest"))
        }

        @Test
        @DisplayName("should have exactly one additional type")
        fun hasExactlyOneAdditionalType() {
            assertEquals(1, handler.additionalTypes.size)
        }
    }

    // ==================== Fill Runner ====================

    @Nested
    @DisplayName("Fill Runner")
    inner class FillRunnerTests {

        @Test
        @DisplayName("should schedule fill runner on main thread")
        fun schedulesOnMainThread() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player
            mockkStatic(Material::class)
            every { Material.matchMaterial("DIAMOND") } returns Material.DIAMOND

            mockSuccessfulChestPlacement()

            // Don't execute the runnable — just capture it
            every { scheduler.runTask(plugin, any<Runnable>()) } returns mockk(relaxed = true)

            val loot = listOf(mapOf("type" to "DIAMOND", "weight" to 1.0, "amount" to 1))
            val params = mapOf<String, Any?>("loot" to loot)

            try {
                handler.handle(context, defaultInvocation, params)
            } catch (_: Throwable) {
                // May fail
            }

            verify { scheduler.runTask(eq(plugin), any<Runnable>()) }
        }

        @Test
        @DisplayName("should log warning when chest inventory not available in fill runner")
        fun logsWarningWhenInventoryUnavailable() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player

            val (_, placeBlock, _) = mockSuccessfulChestPlacement()

            // First call to block.state returns Chest (for placement),
            // second call returns non-Chest (for fill runner)
            every { placeBlock.state } returnsMany listOf(
                mockk<Chest>(relaxed = true),
                mockk(relaxed = true), // Not a Chest — blockInventory won't be available
            )

            val params = mapOf<String, Any?>("debug_force_bread_amount" to 1)

            handler.handle(context, defaultInvocation, params)

            verify { logger.warning(match<String> { it.contains("chest inventory not available") }) }
        }

        @Test
        @DisplayName("should clear inventory before filling")
        fun clearsInventoryBeforeFilling() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player

            val (_, _, chestState) = mockSuccessfulChestPlacement()
            val inventory = mockk<Inventory>(relaxed = true)
            every { chestState.blockInventory } returns inventory
            every { inventory.size } returns 27
            every { inventory.contents } returns arrayOfNulls(27)

            val params = mapOf<String, Any?>("debug_force_bread_amount" to 1)

            try {
                handler.handle(context, defaultInvocation, params)
            } catch (_: Throwable) {
                // ItemStack constructor may fail
            }

            verify { inventory.clear() }
        }

        @Test
        @DisplayName("should access chest state in fill runner")
        fun accessesChestStateInFillRunner() {
            every { ActionUtils.resolveTargetPlayer(any(), any()) } returns player

            val (_, _, chestState) = mockSuccessfulChestPlacement()
            val inventory = mockk<Inventory>(relaxed = true)
            every { chestState.blockInventory } returns inventory
            every { inventory.size } returns 27
            every { inventory.contents } returns arrayOfNulls(27)

            val params = mapOf<String, Any?>("debug_force_bread_amount" to 1)

            try {
                handler.handle(context, defaultInvocation, params)
            } catch (_: Throwable) {
                // ItemStack constructor requires server — expected
            }

            // Verify fill runner accessed blockInventory (happens before ItemStack creation)
            verify { chestState.blockInventory }
        }
    }
}
