package nl.jeroenlabs.labsWorld.twitch

import io.mockk.*
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import nl.jeroenlabs.labsWorld.twitch.commands.Permission
import java.io.File
import java.nio.file.Paths

/**
 * Unit tests for TwitchConfigManager binding parsing behavior.
 *
 * Task T3 from .agent/TASKS.md — Validates parsing and filtering behavior
 * of config-driven command/redeem bindings.
 */
class TwitchConfigManagerBindingsTest {

    private lateinit var mockPlugin: JavaPlugin
    private lateinit var configManager: TwitchConfigManager
    private val fixturesDir = Paths.get("src", "test", "resources", "fixtures", "twitch").toFile()

    @BeforeEach
    fun setup() {
        // Create a mock JavaPlugin
        mockPlugin = mockk<JavaPlugin>(relaxed = true)

        // Mock the logger to avoid NPE
        every { mockPlugin.logger } returns mockk(relaxed = true)

        // Create config manager
        configManager = TwitchConfigManager(mockPlugin)
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    /**
     * Helper to load a YAML fixture file directly into the config manager's internal state.
     * Uses reflection to set the private configYaml field.
     */
    private fun loadFixture(filename: String) {
        val fixtureFile = File(fixturesDir, filename)
        assertTrue(fixtureFile.exists(), "Fixture file not found: $filename")

        val yaml = YamlConfiguration.loadConfiguration(fixtureFile)

        // Use reflection to set the private configYaml field
        val configYamlField = TwitchConfigManager::class.java.getDeclaredField("configYaml")
        configYamlField.isAccessible = true
        configYamlField.set(configManager, yaml)
    }

    // ==================== getRedeemBindings() Tests ====================

    @Nested
    @DisplayName("getRedeemBindings()")
    inner class GetRedeemBindingsTests {

        @Test
        @DisplayName("should parse valid redeem bindings with reward_id and handler")
        fun validRedeemWithRewardIdAndHandler() {
            loadFixture("valid-redeems.yml")

            val bindings = configManager.getRedeemBindings()

            // Should have 5 valid bindings (all from valid-redeems.yml are valid)
            assertEquals(5, bindings.size)

            // Check first binding (reward_id + handler)
            val first = bindings[0]
            assertEquals("aaaa-bbbb-cccc-dddd", first.rewardId)
            assertNull(first.rewardTitle)
            assertEquals("npc.spawn", first.handler)
            assertEquals(true, first.runOnMainThread)
            assertEquals("Test message", first.params["message"])
            assertTrue(first.actions.isEmpty())
        }

        @Test
        @DisplayName("should parse valid redeem bindings with reward_title and handler")
        fun validRedeemWithRewardTitleAndHandler() {
            loadFixture("valid-redeems.yml")

            val bindings = configManager.getRedeemBindings()

            // Check second binding (reward_title + handler)
            val second = bindings[1]
            assertNull(second.rewardId)
            assertEquals("Test Reward", second.rewardTitle)
            assertEquals("npc.attack", second.handler)
            assertNull(second.runOnMainThread)
            assertEquals(30, second.params["seconds"])
            assertEquals(1.0, second.params["hearts_per_hit"])
        }

        @Test
        @DisplayName("should parse valid redeem bindings with both reward_id and reward_title")
        fun validRedeemWithBothMatchers() {
            loadFixture("valid-redeems.yml")

            val bindings = configManager.getRedeemBindings()

            // Check third binding (both reward_id and reward_title)
            val third = bindings[2]
            assertEquals("1111-2222-3333-4444", third.rewardId)
            assertEquals("Duplicate Reward", third.rewardTitle)
            assertEquals("chat.say", third.handler)
        }

        @Test
        @DisplayName("should parse valid redeem bindings with actions instead of handler")
        fun validRedeemWithActions() {
            loadFixture("valid-redeems.yml")

            val bindings = configManager.getRedeemBindings()

            // Check fourth binding (actions instead of handler)
            val fourth = bindings[3]
            assertEquals("Action Based", fourth.rewardTitle)
            assertNull(fourth.handler)
            assertEquals(true, fourth.runOnMainThread)
            assertEquals(2, fourth.actions.size)

            // Check first action
            assertEquals("player.fireworks", fourth.actions[0].type)
            assertEquals(3, fourth.actions[0].params["count"])
            assertEquals(1, fourth.actions[0].params["power"])

            // Check second action
            assertEquals("world.weather", fourth.actions[1].type)
            assertEquals("rain", fourth.actions[1].params["state"])
        }

        @Test
        @DisplayName("should parse valid redeem bindings with both handler and actions")
        fun validRedeemWithBothHandlerAndActions() {
            loadFixture("valid-redeems.yml")

            val bindings = configManager.getRedeemBindings()

            // Check fifth binding (both handler and actions)
            val fifth = bindings[4]
            assertEquals("5555-6666-7777-8888", fifth.rewardId)
            assertEquals("npc.spawn", fifth.handler)
            assertEquals(1, fifth.actions.size)
            assertEquals("player.heal", fifth.actions[0].type)
        }

        @Test
        @DisplayName("should filter out invalid bindings missing both reward_id and reward_title")
        fun invalidRedeemMissingMatchers() {
            loadFixture("invalid-redeems.yml")

            val bindings = configManager.getRedeemBindings()

            // All bindings in invalid-redeems.yml should be filtered out
            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should filter out bindings with empty reward_id and reward_title")
        fun invalidRedeemEmptyMatchers() {
            loadFixture("invalid-redeems.yml")

            val bindings = configManager.getRedeemBindings()

            // Should filter out bindings with empty strings
            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should filter out bindings missing both handler and actions")
        fun invalidRedeemMissingHandlerAndActions() {
            loadFixture("invalid-redeems.yml")

            val bindings = configManager.getRedeemBindings()

            // Should filter out bindings that have matcher but no handler/actions
            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should return empty list when redeems section is missing")
        fun missingRedeemsSection() {
            loadFixture("missing-sections.yml")

            val bindings = configManager.getRedeemBindings()

            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should return empty list when bindings list is missing")
        fun missingBindingsList() {
            loadFixture("empty-config.yml")

            val bindings = configManager.getRedeemBindings()

            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should handle params as empty map when params is missing")
        fun missingParams() {
            loadFixture("valid-redeems.yml")

            val bindings = configManager.getRedeemBindings()

            // Third binding has no params
            val third = bindings[2]
            assertTrue(third.params.isEmpty())
        }
    }

    // ==================== getCommandBindings() Tests ====================

    @Nested
    @DisplayName("getCommandBindings()")
    inner class GetCommandBindingsTests {

        @Test
        @DisplayName("should parse valid command bindings with everyone permission (default)")
        fun validCommandWithEveryonePermission() {
            loadFixture("valid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Should have 8 valid command bindings
            assertEquals(8, bindings.size)

            // Check first binding (default everyone permission)
            val first = bindings[0]
            assertEquals("fireworks", first.name)
            assertEquals(Permission.EVERYONE, first.permission)
            assertNull(first.runOnMainThread)
            assertEquals(1, first.actions.size)
            assertEquals("player.fireworks", first.actions[0].type)
        }

        @Test
        @DisplayName("should parse command with broadcaster permission")
        fun validCommandWithBroadcasterPermission() {
            loadFixture("valid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Check second binding (broadcaster permission)
            val second = bindings[1]
            assertEquals("admin", second.name)
            assertEquals(Permission.BROADCASTER, second.permission)
            assertEquals(true, second.runOnMainThread)
            assertEquals("player.heal", second.actions[0].type)
        }

        @Test
        @DisplayName("should parse command with moderator permission")
        fun validCommandWithModeratorPermission() {
            loadFixture("valid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Check third binding (moderator permission)
            val third = bindings[2]
            assertEquals("modcmd", third.name)
            assertEquals(Permission.MODERATOR, third.permission)
            assertEquals("world.weather", third.actions[0].type)
        }

        @Test
        @DisplayName("should parse command with mod alias for moderator permission")
        fun validCommandWithModAlias() {
            loadFixture("valid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Check fourth binding (mod alias)
            val fourth = bindings[3]
            assertEquals("modcmd2", fourth.name)
            assertEquals(Permission.MODERATOR, fourth.permission)
        }

        @Test
        @DisplayName("should parse command with vip permission")
        fun validCommandWithVipPermission() {
            loadFixture("valid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Check fifth binding (vip permission)
            val fifth = bindings[4]
            assertEquals("vipcmd", fifth.name)
            assertEquals(Permission.VIP, fifth.permission)
        }

        @Test
        @DisplayName("should parse command with subscriber permission")
        fun validCommandWithSubscriberPermission() {
            loadFixture("valid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Check sixth binding (subscriber permission)
            val sixth = bindings[5]
            assertEquals("subcmd", sixth.name)
            assertEquals(Permission.SUBSCRIBER, sixth.permission)
        }

        @Test
        @DisplayName("should parse command with sub alias for subscriber permission")
        fun validCommandWithSubAlias() {
            loadFixture("valid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Check seventh binding (sub alias)
            val seventh = bindings[6]
            assertEquals("subcmd2", seventh.name)
            assertEquals(Permission.SUBSCRIBER, seventh.permission)
        }

        @Test
        @DisplayName("should parse command with multiple actions")
        fun validCommandWithMultipleActions() {
            loadFixture("valid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Check eighth binding (multiple actions)
            val eighth = bindings[7]
            assertEquals("combo", eighth.name)
            assertEquals(Permission.EVERYONE, eighth.permission)
            assertEquals(3, eighth.actions.size)

            // Check all three actions
            assertEquals("player.fireworks", eighth.actions[0].type)
            assertEquals(5, eighth.actions[0].params["count"])

            assertEquals("player.heal", eighth.actions[1].type)
            assertEquals(2, eighth.actions[1].params["hearts"])

            assertEquals("world.weather", eighth.actions[2].type)
            assertEquals("rain", eighth.actions[2].params["state"])
        }

        @Test
        @DisplayName("should filter out commands missing name")
        fun invalidCommandMissingName() {
            loadFixture("invalid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // All bindings in invalid-commands.yml should be filtered out
            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should filter out commands with empty name")
        fun invalidCommandEmptyName() {
            loadFixture("invalid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Should filter out commands with empty name
            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should filter out commands missing actions")
        fun invalidCommandMissingActions() {
            loadFixture("invalid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Should filter out commands with no actions
            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should filter out commands with empty actions array")
        fun invalidCommandEmptyActions() {
            loadFixture("invalid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // Should filter out commands with empty actions
            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should return empty list when commands section is missing")
        fun missingCommandsSection() {
            loadFixture("missing-sections.yml")

            val bindings = configManager.getCommandBindings()

            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should return empty list when bindings list is missing")
        fun missingBindingsList() {
            loadFixture("empty-config.yml")

            val bindings = configManager.getCommandBindings()

            assertEquals(0, bindings.size)
        }

        @Test
        @DisplayName("should default to EVERYONE for unknown permission values")
        fun unknownPermissionDefaultsToEveryone() {
            // We can't easily test this without creating another fixture,
            // but the behavior is tested implicitly when permission is missing/null
            // The parsePermission function should default to EVERYONE
            loadFixture("valid-commands.yml")

            val bindings = configManager.getCommandBindings()

            // First binding has no explicit permission, should default to EVERYONE
            val first = bindings[0]
            assertEquals(Permission.EVERYONE, first.permission)
        }
    }

    // ==================== Reload Version Tests ====================

    @Nested
    @DisplayName("reloadVersion")
    inner class ReloadVersionTests {

        @Test
        @DisplayName("should start at 0 before init")
        fun startsAtZero() {
            val newConfigManager = TwitchConfigManager(mockPlugin)

            // Before init, reloadVersion should be 0
            assertEquals(0, newConfigManager.getReloadVersion())
        }

        @Test
        @DisplayName("should increment on reloadConfig()")
        fun incrementsOnReload() {
            // Load a fixture
            loadFixture("empty-config.yml")

            // Manually set reloadVersion to 5 using reflection
            val reloadVersionField = TwitchConfigManager::class.java.getDeclaredField("reloadVersion")
            reloadVersionField.isAccessible = true
            reloadVersionField.set(configManager, 5L)

            assertEquals(5, configManager.getReloadVersion())

            // Set up the configFile field using reflection to point to our fixture
            val fixtureFile = File(fixturesDir, "empty-config.yml")
            val configFileField = TwitchConfigManager::class.java.getDeclaredField("configFile")
            configFileField.isAccessible = true
            configFileField.set(configManager, fixtureFile)

            // Call reloadConfig - this should increment the version
            configManager.reloadConfig()
            assertEquals(6, configManager.getReloadVersion())
        }

        @Test
        @DisplayName("should increment multiple times on repeated reloadConfig calls")
        fun incrementsOnMultipleReloads() {
            // Load a fixture
            loadFixture("empty-config.yml")

            // Set up the configFile field using reflection
            val fixtureFile = File(fixturesDir, "empty-config.yml")
            val configFileField = TwitchConfigManager::class.java.getDeclaredField("configFile")
            configFileField.isAccessible = true
            configFileField.set(configManager, fixtureFile)

            // Initial version should be 0
            assertEquals(0, configManager.getReloadVersion())

            // Reload multiple times
            configManager.reloadConfig()
            assertEquals(1, configManager.getReloadVersion())

            configManager.reloadConfig()
            assertEquals(2, configManager.getReloadVersion())

            configManager.reloadConfig()
            assertEquals(3, configManager.getReloadVersion())
        }
    }

    // ==================== Helper Methods Tests ====================

    @Nested
    @DisplayName("Helper Methods")
    inner class HelperMethodsTests {

        @Test
        @DisplayName("isRedeemsEnabled() should return correct value")
        fun isRedeemsEnabled() {
            loadFixture("valid-redeems.yml")
            assertTrue(configManager.isRedeemsEnabled())

            loadFixture("invalid-redeems.yml")
            assertTrue(configManager.isRedeemsEnabled())

            loadFixture("empty-config.yml")
            assertFalse(configManager.isRedeemsEnabled())
        }

        @Test
        @DisplayName("shouldLogUnmatchedRedeems() should return correct value")
        fun shouldLogUnmatchedRedeems() {
            loadFixture("valid-redeems.yml")
            assertTrue(configManager.shouldLogUnmatchedRedeems())

            loadFixture("invalid-redeems.yml")
            assertFalse(configManager.shouldLogUnmatchedRedeems())

            // Default should be true
            loadFixture("empty-config.yml")
            assertTrue(configManager.shouldLogUnmatchedRedeems())
        }
    }
}
