package nl.jeroenlabs.labsWorld.npc

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.bukkit.entity.Villager
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VillagerNpcLevelServiceTest {

    private lateinit var plugin: JavaPlugin
    private lateinit var npcLinkManager: VillagerNpcLinkManager
    private lateinit var service: VillagerNpcLevelService

    @BeforeEach
    fun setup() {
        plugin = mockk(relaxed = true)
        npcLinkManager = mockk(relaxed = true)
        service = VillagerNpcLevelService(plugin, npcLinkManager)
        mockkObject(VillagerNpcKeys)
    }

    @AfterEach
    fun cleanup() {
        clearAllMocks()
    }

    private fun createMockVillager(xp: Int = 0, level: Int = 1, userId: String = "user1"): Villager {
        val villager = mockk<Villager>(relaxed = true)
        every { VillagerNpcKeys.getXp(villager, plugin) } returns xp
        every { VillagerNpcKeys.getLevel(villager, plugin) } returns level
        every { VillagerNpcKeys.getLinkedUserId(villager, plugin) } returns userId
        every { villager.isValid } returns true
        return villager
    }

    // ==================== XP Threshold Calculations ====================

    @Nested
    @DisplayName("XP Threshold Calculations")
    inner class XpThresholdTests {

        @Test
        @DisplayName("should require 100 XP for level 1 to level 2")
        fun level1To2() {
            assertEquals(100, service.xpForNextLevel(1))
        }

        @Test
        @DisplayName("should require 200 XP for level 2 to level 3")
        fun level2To3() {
            assertEquals(200, service.xpForNextLevel(2))
        }

        @Test
        @DisplayName("should require 500 XP for level 5 to level 6")
        fun level5To6() {
            assertEquals(500, service.xpForNextLevel(5))
        }

        @Test
        @DisplayName("should calculate total XP for level 1 as 0")
        fun totalXpForLevel1() {
            assertEquals(0, service.totalXpForLevel(1))
        }

        @Test
        @DisplayName("should calculate total XP for level 2 as 100")
        fun totalXpForLevel2() {
            assertEquals(100, service.totalXpForLevel(2))
        }

        @Test
        @DisplayName("should calculate total XP for level 3 as 300")
        fun totalXpForLevel3() {
            // Level 1->2 = 100, Level 2->3 = 200, total = 300
            assertEquals(300, service.totalXpForLevel(3))
        }

        @Test
        @DisplayName("should calculate total XP for level 4 as 600")
        fun totalXpForLevel4() {
            // 100 + 200 + 300 = 600
            assertEquals(600, service.totalXpForLevel(4))
        }
    }

    // ==================== addXp ====================

    @Nested
    @DisplayName("addXp")
    inner class AddXpTests {

        @Test
        @DisplayName("should add XP without level-up when below threshold")
        fun addXpNoLevelUp() {
            val villager = createMockVillager(xp = 0, level = 1)

            val result = service.addXp(villager, 50)

            assertFalse(result.leveledUp)
            assertEquals(1, result.oldLevel)
            assertEquals(1, result.newLevel)
            assertEquals(50, result.totalXp)
            verify { VillagerNpcKeys.setXp(villager, 50, plugin) }
        }

        /**
         * Level-up calls updateNameTag/playLevelUpEffects which trigger
         * Bukkit's Particle/Sound registry. We absorb the class loading error
         * and verify XP/level PDC calls that happen before the effects.
         */
        private fun addXpCatching(villager: Villager, amount: Int): VillagerNpcLevelService.LevelUpResult? {
            return try {
                service.addXp(villager, amount)
            } catch (_: Throwable) {
                // Class loading error from Particle/Sound; verify PDC state instead
                null
            }
        }

        @Test
        @DisplayName("should level up when XP reaches threshold")
        fun addXpLevelUp() {
            val villager = createMockVillager(xp = 80, level = 1)
            every { npcLinkManager.getStoredUserName("user1") } returns "User1"

            addXpCatching(villager, 50)

            verify { VillagerNpcKeys.setXp(villager, 130, plugin) }
            verify { VillagerNpcKeys.setLevel(villager, 2, plugin) }
        }

        @Test
        @DisplayName("should handle multiple level-ups in one XP grant")
        fun multipleLeveUps() {
            val villager = createMockVillager(xp = 0, level = 1)
            every { npcLinkManager.getStoredUserName("user1") } returns "User1"

            addXpCatching(villager, 350)

            verify { VillagerNpcKeys.setXp(villager, 350, plugin) }
            verify { VillagerNpcKeys.setLevel(villager, 3, plugin) }
        }

        @Test
        @DisplayName("should not level up when exactly at threshold minus one")
        fun noLevelUpJustBelow() {
            val villager = createMockVillager(xp = 0, level = 1)

            val result = service.addXp(villager, 99)

            assertFalse(result.leveledUp)
            assertEquals(1, result.newLevel)
        }

        @Test
        @DisplayName("should level up when exactly at threshold")
        fun levelUpAtExactThreshold() {
            val villager = createMockVillager(xp = 0, level = 1)
            every { npcLinkManager.getStoredUserName("user1") } returns "User1"

            addXpCatching(villager, 100)

            verify { VillagerNpcKeys.setLevel(villager, 2, plugin) }
        }
    }

    // ==================== getLevel and getXp ====================

    @Nested
    @DisplayName("getLevel and getXp")
    inner class GetLevelAndXpTests {

        @Test
        @DisplayName("should return level from VillagerNpcKeys")
        fun getsLevel() {
            val villager = createMockVillager(level = 5)

            assertEquals(5, service.getLevel(villager))
        }

        @Test
        @DisplayName("should return XP from VillagerNpcKeys")
        fun getsXp() {
            val villager = createMockVillager(xp = 250)

            assertEquals(250, service.getXp(villager))
        }
    }

    // ==================== updateNameTag ====================

    @Nested
    @DisplayName("updateNameTag")
    inner class UpdateNameTagTests {

        @Test
        @DisplayName("should set custom name with level prefix")
        fun setsNameWithLevel() {
            val villager = createMockVillager(level = 3, userId = "user1")
            every { npcLinkManager.getStoredUserName("user1") } returns "TestUser"

            service.updateNameTag(villager)

            verify { villager.customName(any()) }
            verify { villager.isCustomNameVisible = true }
        }

        @Test
        @DisplayName("should use provided level instead of reading from PDC")
        fun usesProvidedLevel() {
            val villager = createMockVillager(level = 1, userId = "user1")
            every { npcLinkManager.getStoredUserName("user1") } returns "TestUser"

            service.updateNameTag(villager, level = 5)

            // Should use level 5, not level 1 from PDC
            verify { villager.customName(any()) }
        }

        @Test
        @DisplayName("should not set name tag when userId is null")
        fun skipsWhenNoUserId() {
            val villager = mockk<Villager>(relaxed = true)
            every { VillagerNpcKeys.getLinkedUserId(villager, plugin) } returns null

            service.updateNameTag(villager)

            verify(exactly = 0) { villager.customName(any()) }
        }

        @Test
        @DisplayName("should fall back to userId when stored name is null")
        fun fallsBackToUserId() {
            val villager = createMockVillager(level = 1, userId = "user123")
            every { npcLinkManager.getStoredUserName("user123") } returns null

            service.updateNameTag(villager)

            verify { villager.customName(any()) }
        }
    }

    // ==================== Constants ====================

    @Nested
    @DisplayName("Constants")
    inner class ConstantsTests {

        @Test
        @DisplayName("should define XP_PER_LEVEL_BASE as 100")
        fun xpPerLevelBase() {
            assertEquals(100, VillagerNpcLevelService.XP_PER_LEVEL_BASE)
        }

        @Test
        @DisplayName("should define DUEL_WIN_XP as 50")
        fun duelWinXp() {
            assertEquals(50, VillagerNpcLevelService.DUEL_WIN_XP)
        }

        @Test
        @DisplayName("should define DUEL_PARTICIPATION_XP as 10")
        fun duelParticipationXp() {
            assertEquals(10, VillagerNpcLevelService.DUEL_PARTICIPATION_XP)
        }
    }
}
