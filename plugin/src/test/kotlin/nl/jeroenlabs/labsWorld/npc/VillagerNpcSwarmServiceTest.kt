package nl.jeroenlabs.labsWorld.npc

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class VillagerNpcSwarmServiceTest {

    private lateinit var plugin: JavaPlugin
    private lateinit var linkManager: VillagerNpcLinkManager
    private lateinit var scheduler: BukkitScheduler
    private lateinit var service: VillagerNpcSwarmService
    private lateinit var target: Player
    private lateinit var world: World

    @BeforeEach
    fun setup() {
        plugin = mockk(relaxed = true)
        linkManager = mockk(relaxed = true)
        scheduler = mockk(relaxed = true)
        world = mockk(relaxed = true)
        target = mockk(relaxed = true)

        every { plugin.server.scheduler } returns scheduler
        every { target.world } returns world
        every { target.isOnline } returns true
        every { world.uid } returns UUID.randomUUID()
        every { linkManager.findAllLinkedVillagerNpcs() } returns emptyList()

        service = VillagerNpcSwarmService(plugin, linkManager)
    }

    @AfterEach
    fun cleanup() {
        clearAllMocks()
    }

    // ==================== StartSwarm Validation ====================

    @Nested
    @DisplayName("StartSwarm Validation")
    inner class StartSwarmValidationTests {

        @Test
        @DisplayName("should fail when durationSeconds is zero")
        fun failsWhenDurationZero() {
            val result = service.startSwarm(target, durationSeconds = 0)

            assertTrue(result.isFailure)
            assertEquals("durationSeconds must be > 0", result.exceptionOrNull()?.message)
        }

        @Test
        @DisplayName("should fail when durationSeconds is negative")
        fun failsWhenDurationNegative() {
            val result = service.startSwarm(target, durationSeconds = -5)

            assertTrue(result.isFailure)
            assertEquals("durationSeconds must be > 0", result.exceptionOrNull()?.message)
        }

        @Test
        @DisplayName("should return zero when no NPCs exist")
        fun returnsZeroWhenNoNpcs() {
            every { linkManager.findAllLinkedVillagerNpcs() } returns emptyList()

            val result = service.startSwarm(target)

            assertTrue(result.isSuccess)
            assertEquals(0, result.getOrNull())
        }
    }

    // ==================== isActive Property ====================

    @Nested
    @DisplayName("isActive Property")
    inner class IsActiveTests {

        @Test
        @DisplayName("should be false when no task exists")
        fun falseWhenNoTask() {
            assertFalse(service.isActive)
        }

        @Test
        @DisplayName("should be true when task is running")
        fun trueWhenTaskRunning() {
            val task = mockk<BukkitTask>(relaxed = true)
            every { task.isCancelled } returns false

            val taskField = VillagerNpcSwarmService::class.java.getDeclaredField("runningTask")
            taskField.isAccessible = true
            taskField.set(service, task)

            assertTrue(service.isActive)
        }

        @Test
        @DisplayName("should be false when task is cancelled")
        fun falseWhenTaskCancelled() {
            val task = mockk<BukkitTask>(relaxed = true)
            every { task.isCancelled } returns true

            val taskField = VillagerNpcSwarmService::class.java.getDeclaredField("runningTask")
            taskField.isAccessible = true
            taskField.set(service, task)

            assertFalse(service.isActive)
        }
    }

    // ==================== Concurrent Swarm Rejection ====================

    @Nested
    @DisplayName("Concurrent Swarm Rejection")
    inner class ConcurrentSwarmRejectionTests {

        @Test
        @DisplayName("should reject new swarm when one is already active")
        fun rejectsWhenActive() {
            val existingTask = mockk<BukkitTask>(relaxed = true)
            every { existingTask.isCancelled } returns false

            val taskField = VillagerNpcSwarmService::class.java.getDeclaredField("runningTask")
            taskField.isAccessible = true
            taskField.set(service, existingTask)

            val result = service.startSwarm(target)

            assertTrue(result.isFailure)
            assertEquals("A swarm is already in progress", result.exceptionOrNull()?.message)
        }

        @Test
        @DisplayName("should not cancel existing task when new swarm is rejected")
        fun doesNotCancelExisting() {
            val existingTask = mockk<BukkitTask>(relaxed = true)
            every { existingTask.isCancelled } returns false

            val taskField = VillagerNpcSwarmService::class.java.getDeclaredField("runningTask")
            taskField.isAccessible = true
            taskField.set(service, existingTask)

            service.startSwarm(target)

            verify(exactly = 0) { existingTask.cancel() }
        }

        @Test
        @DisplayName("should allow new swarm after previous task is cancelled")
        fun allowsAfterCancelled() {
            val existingTask = mockk<BukkitTask>(relaxed = true)
            every { existingTask.isCancelled } returns true

            val taskField = VillagerNpcSwarmService::class.java.getDeclaredField("runningTask")
            taskField.isAccessible = true
            taskField.set(service, existingTask)

            val result = service.startSwarm(target)

            // Should pass the isActive check — returns 0 because no NPCs
            assertTrue(result.isSuccess)
            assertEquals(0, result.getOrNull())
        }
    }

    // ==================== Stop ====================

    @Nested
    @DisplayName("Stop")
    inner class StopTests {

        @Test
        @DisplayName("should clear isActive when stop is called")
        fun clearsIsActiveOnStop() {
            val task = mockk<BukkitTask>(relaxed = true)
            every { task.isCancelled } returns false

            val taskField = VillagerNpcSwarmService::class.java.getDeclaredField("runningTask")
            taskField.isAccessible = true
            taskField.set(service, task)

            assertTrue(service.isActive)

            service.stop()

            assertFalse(service.isActive)
        }

        @Test
        @DisplayName("should cancel the running task when stop is called")
        fun cancelsTaskOnStop() {
            val task = mockk<BukkitTask>(relaxed = true)
            every { task.isCancelled } returns false

            val taskField = VillagerNpcSwarmService::class.java.getDeclaredField("runningTask")
            taskField.isAccessible = true
            taskField.set(service, task)

            service.stop()

            verify { task.cancel() }
        }
    }
}
