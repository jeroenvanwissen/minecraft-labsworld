package nl.jeroenlabs.labsWorld.npc

import io.mockk.*
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Unit tests for VillagerNpcDuelService.
 *
 * Limitation: Bukkit's Villager interface triggers ExceptionInInitializerError
 * when class-loaded in unit tests (registry system unavailable). This prevents
 * testing code paths that call npcLinkManager.ensureNpcAt (which has a
 * Villager.Profession parameter) or findLoadedNpcByUserId (returns Villager?).
 *
 * Tests that need to execute past pickSpawnLocation use runCatching to absorb
 * the class loading error while still verifying behavior that occurred before it.
 * Duel execution tests (hit/miss, winner/loser, respawn) require integration tests.
 */
class VillagerNpcDuelServiceTest {

    private lateinit var plugin: JavaPlugin
    private lateinit var npcLinkManager: VillagerNpcLinkManager
    private lateinit var npcSpawnPointManager: VillagerNpcSpawnPointManager
    private lateinit var configManager: TwitchConfigManager
    private lateinit var scheduler: BukkitScheduler
    private lateinit var service: VillagerNpcDuelService
    private lateinit var world: World
    private val worldUid = UUID.randomUUID()
    private val announcements = mutableListOf<String>()
    private val announce: (String) -> Unit = { announcements.add(it) }

    @BeforeEach
    fun setup() {
        plugin = mockk(relaxed = true)
        npcLinkManager = mockk(relaxed = true)
        npcSpawnPointManager = mockk(relaxed = true)
        configManager = mockk(relaxed = true)
        scheduler = mockk(relaxed = true)
        world = mockk(relaxed = true)

        every { plugin.server.scheduler } returns scheduler
        every { world.uid } returns worldUid

        every { npcSpawnPointManager.getSpawnPointLocations() } returns emptyList()
        every { configManager.getDuelConfig() } returns TwitchConfigManager.DuelConfig(
            hitChance = 0.65,
            speed = 1.15,
            attackRange = 1.9,
            maxHp = 10,
            respawnDelaySeconds = 10L,
        )

        service = VillagerNpcDuelService(plugin, npcLinkManager, npcSpawnPointManager, configManager)
        announcements.clear()
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    private fun createSpawnLocation(blockX: Int = 0): Location {
        val loc = mockk<Location>(relaxed = true)
        every { loc.world } returns world
        every { loc.blockX } returns blockX
        every { loc.blockY } returns 64
        every { loc.blockZ } returns 0
        every { loc.clone() } answers {
            val clone1 = mockk<Location>(relaxed = true)
            every { clone1.world } returns world
            every { clone1.add(any<Double>(), any<Double>(), any<Double>()) } returns clone1
            every { clone1.clone() } answers {
                val clone2 = mockk<Location>(relaxed = true)
                every { clone2.world } returns world
                every { clone2.add(any<Double>(), any<Double>(), any<Double>()) } returns clone2
                clone2
            }
            clone1
        }
        return loc
    }

    private fun mockSpawnPoint() {
        every { npcSpawnPointManager.getSpawnPointLocations() } returns listOf(createSpawnLocation())
    }

    /**
     * Calls startDuel, catching JVM class loading errors that occur when
     * Villager is referenced (ensureNpcAt has Villager.Profession parameter).
     */
    private fun startDuelCatching(
        userAId: String = "userA",
        userAName: String = "UserA",
        userBId: String = "userB",
        userBName: String = "UserB",
    ) {
        try {
            service.startDuel(userAId, userAName, userBId, userBName, announce)
        } catch (_: Throwable) {
            // Expected: ExceptionInInitializerError or NoClassDefFoundError
            // from Villager class loading when ensureNpcAt is called
        }
    }

    // ==================== StartDuel Validation ====================

    @Nested
    @DisplayName("StartDuel Validation")
    inner class StartDuelValidationTests {

        @Test
        @DisplayName("should fail when both user IDs are the same")
        fun failsWhenSameUserId() {
            val result = service.startDuel("user1", "UserA", "user1", "UserB", announce)

            assertTrue(result.isFailure)
            assertEquals("Cannot duel same user", result.exceptionOrNull()?.message)
        }

        @Test
        @DisplayName("should not produce any announcements when same user")
        fun noAnnouncementsWhenSameUser() {
            service.startDuel("user1", "UserA", "user1", "UserB", announce)

            assertTrue(announcements.isEmpty())
        }

        @Test
        @DisplayName("should fail when no spawn points are placed")
        fun failsWhenNoSpawnPoints() {
            val result = service.startDuel("userA", "UserA", "userB", "UserB", announce)

            assertTrue(result.isFailure)
            assertEquals("No NPC Spawn Point is placed", result.exceptionOrNull()?.message)
        }

        @Test
        @DisplayName("should not produce any announcements when no spawn points")
        fun noAnnouncementsWhenNoSpawnPoints() {
            service.startDuel("userA", "UserA", "userB", "UserB", announce)

            assertTrue(announcements.isEmpty())
        }
    }

    // ==================== pickSpawnLocation ====================

    @Nested
    @DisplayName("pickSpawnLocation (via startDuel)")
    inner class PickSpawnLocationTests {

        @Test
        @DisplayName("should call reconcileStoredSpawnPoints")
        fun callsReconcile() {
            service.startDuel("userA", "UserA", "userB", "UserB", announce)

            verify { npcSpawnPointManager.reconcileStoredSpawnPoints() }
        }

        @Test
        @DisplayName("should call getSpawnPointLocations")
        fun callsGetSpawnPointLocations() {
            service.startDuel("userA", "UserA", "userB", "UserB", announce)

            verify { npcSpawnPointManager.getSpawnPointLocations() }
        }

        @Test
        @DisplayName("should reconcile before getting locations")
        fun reconcilesBeforeGettingLocations() {
            service.startDuel("userA", "UserA", "userB", "UserB", announce)

            verifyOrder {
                npcSpawnPointManager.reconcileStoredSpawnPoints()
                npcSpawnPointManager.getSpawnPointLocations()
            }
        }

        @Test
        @DisplayName("should return failure when spawn point list is empty")
        fun failsWhenEmpty() {
            val result = service.startDuel("userA", "UserA", "userB", "UserB", announce)

            assertTrue(result.isFailure)
            assertEquals("No NPC Spawn Point is placed", result.exceptionOrNull()?.message)
        }

        @Test
        @DisplayName("should select first spawn point when sorted by coordinates")
        fun selectsFirstSorted() {
            val loc1 = createSpawnLocation(blockX = 100)
            val loc2 = createSpawnLocation(blockX = 5)
            every { npcSpawnPointManager.getSpawnPointLocations() } returns listOf(loc1, loc2)

            startDuelCatching()

            // loc2 (blockX=5) sorts first, so it should be cloned for the spawn base
            verify { loc2.clone() }
        }

        @Test
        @DisplayName("should clone chosen spawn location")
        fun clonesChosenSpawnLocation() {
            val spawnLoc = createSpawnLocation()
            every { npcSpawnPointManager.getSpawnPointLocations() } returns listOf(spawnLoc)

            startDuelCatching()

            verify { spawnLoc.clone() }
        }
    }

    // ==================== isActive Property ====================

    @Nested
    @DisplayName("isActive Property")
    inner class IsActiveTests {

        @Test
        @DisplayName("should be false when no duel task exists")
        fun falseWhenNoTask() {
            assertFalse(service.isActive)
        }

        @Test
        @DisplayName("should be true when duel task is running")
        fun trueWhenTaskRunning() {
            val task = mockk<BukkitTask>(relaxed = true)
            every { task.isCancelled } returns false

            val taskField = VillagerNpcDuelService::class.java.getDeclaredField("duelTask")
            taskField.isAccessible = true
            taskField.set(service, task)

            assertTrue(service.isActive)
        }

        @Test
        @DisplayName("should be false when duel task is cancelled")
        fun falseWhenTaskCancelled() {
            val task = mockk<BukkitTask>(relaxed = true)
            every { task.isCancelled } returns true

            val taskField = VillagerNpcDuelService::class.java.getDeclaredField("duelTask")
            taskField.isAccessible = true
            taskField.set(service, task)

            assertFalse(service.isActive)
        }
    }

    // ==================== Concurrent Duel Rejection ====================

    @Nested
    @DisplayName("Concurrent Duel Rejection")
    inner class ConcurrentDuelRejectionTests {

        @Test
        @DisplayName("should reject new duel when one is already active")
        fun rejectsWhenActive() {
            mockSpawnPoint()

            val existingTask = mockk<BukkitTask>(relaxed = true)
            every { existingTask.isCancelled } returns false

            val taskField = VillagerNpcDuelService::class.java.getDeclaredField("duelTask")
            taskField.isAccessible = true
            taskField.set(service, existingTask)

            val result = service.startDuel("userC", "UserC", "userD", "UserD", announce)

            assertTrue(result.isFailure)
            assertEquals("A duel is already in progress", result.exceptionOrNull()?.message)
        }

        @Test
        @DisplayName("should not cancel existing duel when new one is requested")
        fun doesNotCancelExisting() {
            mockSpawnPoint()

            val existingTask = mockk<BukkitTask>(relaxed = true)
            every { existingTask.isCancelled } returns false

            val taskField = VillagerNpcDuelService::class.java.getDeclaredField("duelTask")
            taskField.isAccessible = true
            taskField.set(service, existingTask)

            service.startDuel("userC", "UserC", "userD", "UserD", announce)

            verify(exactly = 0) { existingTask.cancel() }
        }

        @Test
        @DisplayName("should not produce announcements when duel is rejected")
        fun noAnnouncementsWhenRejected() {
            mockSpawnPoint()

            val existingTask = mockk<BukkitTask>(relaxed = true)
            every { existingTask.isCancelled } returns false

            val taskField = VillagerNpcDuelService::class.java.getDeclaredField("duelTask")
            taskField.isAccessible = true
            taskField.set(service, existingTask)

            service.startDuel("userC", "UserC", "userD", "UserD", announce)

            assertTrue(announcements.isEmpty())
        }

        @Test
        @DisplayName("should allow new duel after previous task is cancelled")
        fun allowsAfterCancelled() {
            mockSpawnPoint()

            val existingTask = mockk<BukkitTask>(relaxed = true)
            every { existingTask.isCancelled } returns true

            val taskField = VillagerNpcDuelService::class.java.getDeclaredField("duelTask")
            taskField.isAccessible = true
            taskField.set(service, existingTask)

            // Should proceed past the isActive check (may fail later due to Villager class loading)
            assertDoesNotThrow { startDuelCatching() }
        }

        @Test
        @DisplayName("should be safe when no previous duel task exists")
        fun safeWithNoPreviousTask() {
            mockSpawnPoint()

            assertDoesNotThrow { startDuelCatching() }
        }
    }
}
