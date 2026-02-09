package nl.jeroenlabs.labsWorld.npc

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.util.UUID
import java.util.logging.Logger

class VillagerNpcSpawnPointManagerTest {

    private lateinit var plugin: JavaPlugin
    private lateinit var logger: Logger
    private lateinit var tempDir: File
    private lateinit var manager: VillagerNpcSpawnPointManager

    @BeforeEach
    fun setup() {
        plugin = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        tempDir = createTempDir("spawnpoint-test")

        every { plugin.dataFolder } returns tempDir
        every { plugin.logger } returns logger

        mockkStatic(Bukkit::class)

        manager = VillagerNpcSpawnPointManager(plugin)
    }

    @AfterEach
    fun cleanup() {
        clearAllMocks()
        tempDir.deleteRecursively()
    }

    // ==================== pickSpawnLocation Error Messages ====================

    @Nested
    @DisplayName("pickSpawnLocation Error Messages")
    inner class PickSpawnLocationErrorTests {

        @Test
        @DisplayName("should return failure with 'No spawn points configured' when no spawn points exist")
        fun failsWhenNoSpawnPoints() {
            // Arrange — init with empty storage
            manager.init()

            // Act
            val result = manager.pickSpawnLocation()

            // Assert
            assertTrue(result.isFailure)
            val msg = result.exceptionOrNull()?.message ?: ""
            assertTrue(msg.contains("No spawn points configured"), "Error should mention no spawn points configured, got: $msg")
        }

        @Test
        @DisplayName("should log warning when no spawn points configured")
        fun logsWarningWhenNoSpawnPoints() {
            manager.init()

            manager.pickSpawnLocation()

            verify { logger.warning(match<String> { it.contains("No spawn points configured") }) }
        }

        @Test
        @DisplayName("should return failure with 'No spawn points in valid worlds' when worlds are unloaded")
        fun failsWhenWorldsUnloaded() {
            // Arrange — write a spawn point with a world UUID that doesn't resolve
            val fakeWorldId = UUID.randomUUID()
            val storageFile = File(tempDir, "npc-spawnpoints.yml")
            storageFile.writeText("spawn_points:\n- '$fakeWorldId:10:64:20'\n")

            every { Bukkit.getWorld(fakeWorldId) } returns null

            manager.init()

            // Act
            val result = manager.pickSpawnLocation()

            // Assert
            assertTrue(result.isFailure)
            val msg = result.exceptionOrNull()?.message ?: ""
            assertTrue(msg.contains("No spawn points in valid worlds"), "Error should mention invalid worlds, got: $msg")
            assertTrue(msg.contains("1 spawn point(s) stored"), "Error should mention count of stored points, got: $msg")
        }

        @Test
        @DisplayName("should log warning when worlds are unloaded")
        fun logsWarningWhenWorldsUnloaded() {
            val fakeWorldId = UUID.randomUUID()
            val storageFile = File(tempDir, "npc-spawnpoints.yml")
            storageFile.writeText("spawn_points:\n- '$fakeWorldId:10:64:20'\n")

            every { Bukkit.getWorld(fakeWorldId) } returns null

            manager.init()

            manager.pickSpawnLocation()

            verify { logger.warning(match<String> { it.contains("No spawn points in valid worlds") }) }
        }

        @Test
        @DisplayName("should return success with location when spawn point is valid")
        fun succeedsWhenValid() {
            val worldId = UUID.randomUUID()
            val world = mockk<World>(relaxed = true)
            every { world.uid } returns worldId

            val storageFile = File(tempDir, "npc-spawnpoints.yml")
            storageFile.writeText("spawn_points:\n- '$worldId:10:64:20'\n")

            every { Bukkit.getWorld(worldId) } returns world

            // Mock the block at the spawn point so reconcile doesn't remove it.
            // The reconcile checks isSpawnPointBlock which checks PDC + registry.
            // Since our spawn point is in the registry (loaded from file), it will pass.
            val block = mockk<org.bukkit.block.Block>(relaxed = true)
            every { world.getBlockAt(10, 64, 20) } returns block
            val blockLocation = mockk<Location>(relaxed = true)
            every { block.location } returns blockLocation
            every { blockLocation.world } returns world
            every { blockLocation.blockX } returns 10
            every { blockLocation.blockY } returns 64
            every { blockLocation.blockZ } returns 20
            every { world.uid } returns worldId

            manager.init()

            val result = manager.pickSpawnLocation()

            assertTrue(result.isSuccess, "Should succeed when spawn point is valid, but got: ${result.exceptionOrNull()?.message}")
        }
    }
}
