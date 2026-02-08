package nl.jeroenlabs.labsWorld.twitch

import io.mockk.*
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TwitchClientManagerTest {

    private lateinit var plugin: JavaPlugin
    private lateinit var twitchConfigManager: TwitchConfigManager
    private lateinit var manager: TwitchClientManager

    @BeforeEach
    fun setup() {
        plugin = mockk<JavaPlugin>(relaxed = true)
        twitchConfigManager = mockk<TwitchConfigManager>(relaxed = true)
        manager = TwitchClientManager(plugin, twitchConfigManager)
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    private fun mockConfig(
        clientId: String? = "test-client-id",
        clientSecret: String? = "test-client-secret",
        channelName: String? = "test-channel",
        accessToken: String? = null,
        refreshToken: String? = null,
    ) {
        every { twitchConfigManager.getConfig() } returns TwitchConfigManager.TwitchConfig(
            clientId = clientId,
            clientSecret = clientSecret,
            channelName = channelName,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    // ==================== Init ====================

    @Nested
    @DisplayName("Init")
    inner class InitTests {

        @Test
        @DisplayName("should return false when hasRequiredConfig is false")
        fun returnsFalseWhenMissingRequiredConfig() {
            every { twitchConfigManager.hasRequiredConfig() } returns false

            val result = manager.init()

            assertFalse(result)
            verify { plugin.logger.severe(match<String> { it.contains("Missing Twitch configuration") }) }
        }

        @Test
        @DisplayName("should return false when clientId is empty")
        fun returnsFalseWhenClientIdEmpty() {
            every { twitchConfigManager.hasRequiredConfig() } returns true
            mockConfig(clientId = "")

            val result = manager.init()

            assertFalse(result)
            verify { plugin.logger.severe(match<String> { it.contains("Missing required Twitch configuration") }) }
        }

        @Test
        @DisplayName("should return false when clientId is null")
        fun returnsFalseWhenClientIdNull() {
            every { twitchConfigManager.hasRequiredConfig() } returns true
            mockConfig(clientId = null)

            val result = manager.init()

            assertFalse(result)
            verify { plugin.logger.severe(match<String> { it.contains("Missing required Twitch configuration") }) }
        }

        @Test
        @DisplayName("should return false when clientSecret is empty")
        fun returnsFalseWhenClientSecretEmpty() {
            every { twitchConfigManager.hasRequiredConfig() } returns true
            mockConfig(clientSecret = "")

            val result = manager.init()

            assertFalse(result)
            verify { plugin.logger.severe(match<String> { it.contains("Missing required Twitch configuration") }) }
        }

        @Test
        @DisplayName("should return false when clientSecret is null")
        fun returnsFalseWhenClientSecretNull() {
            every { twitchConfigManager.hasRequiredConfig() } returns true
            mockConfig(clientSecret = null)

            val result = manager.init()

            assertFalse(result)
            verify { plugin.logger.severe(match<String> { it.contains("Missing required Twitch configuration") }) }
        }

        @Test
        @DisplayName("should return false when channelName is empty")
        fun returnsFalseWhenChannelNameEmpty() {
            every { twitchConfigManager.hasRequiredConfig() } returns true
            mockConfig(channelName = "")

            val result = manager.init()

            assertFalse(result)
            verify { plugin.logger.severe(match<String> { it.contains("Missing required Twitch configuration") }) }
        }

        @Test
        @DisplayName("should return false when channelName is null")
        fun returnsFalseWhenChannelNameNull() {
            every { twitchConfigManager.hasRequiredConfig() } returns true
            mockConfig(channelName = null)

            val result = manager.init()

            assertFalse(result)
            verify { plugin.logger.severe(match<String> { it.contains("Missing required Twitch configuration") }) }
        }

        @Test
        @DisplayName("should return false when access token is empty and no refresh token")
        fun returnsFalseWhenEmptyTokenAndNoRefresh() {
            every { twitchConfigManager.hasRequiredConfig() } returns true
            mockConfig(accessToken = "", refreshToken = null)

            val result = manager.init()

            assertFalse(result)
            verify {
                plugin.logger.warning(match<String> { it.contains("No valid access token and no refresh token") })
            }
        }

        @Test
        @DisplayName("should return false when access token is null and no refresh token")
        fun returnsFalseWhenNullTokenAndNoRefresh() {
            every { twitchConfigManager.hasRequiredConfig() } returns true
            mockConfig(accessToken = null, refreshToken = null)

            val result = manager.init()

            assertFalse(result)
            verify {
                plugin.logger.warning(match<String> { it.contains("No valid access token and no refresh token") })
            }
        }

        @Test
        @DisplayName("should return false when access token is short and no refresh token")
        fun returnsFalseWhenShortTokenAndNoRefresh() {
            every { twitchConfigManager.hasRequiredConfig() } returns true
            mockConfig(accessToken = "short", refreshToken = null)

            val result = manager.init()

            assertFalse(result)
            verify {
                plugin.logger.warning(match<String> { it.contains("No valid access token and no refresh token") })
            }
        }

        @Test
        @DisplayName("should return false when access token is exactly 10 chars and no refresh token")
        fun returnsFalseWhenTenCharTokenAndNoRefresh() {
            every { twitchConfigManager.hasRequiredConfig() } returns true
            mockConfig(accessToken = "1234567890", refreshToken = null)

            val result = manager.init()

            assertFalse(result)
            verify {
                plugin.logger.warning(match<String> { it.contains("No valid access token and no refresh token") })
            }
        }
    }

    // ==================== IsConnected ====================

    @Nested
    @DisplayName("IsConnected")
    inner class IsConnectedTests {

        @Test
        @DisplayName("should return false before init")
        fun returnsFalseBeforeInit() {
            assertFalse(manager.isConnected())
        }

        @Test
        @DisplayName("should return false after failed init")
        fun returnsFalseAfterFailedInit() {
            every { twitchConfigManager.hasRequiredConfig() } returns false

            manager.init()

            assertFalse(manager.isConnected())
        }
    }

    // ==================== Close ====================

    @Nested
    @DisplayName("Close")
    inner class CloseTests {

        @Test
        @DisplayName("should be safe to call before init")
        fun safeToCallBeforeInit() {
            assertDoesNotThrow { manager.close() }
        }

        @Test
        @DisplayName("should cancel token monitor task")
        fun cancelsTokenMonitorTask() {
            val mockTask = mockk<BukkitTask>(relaxed = true)

            val taskField = TwitchClientManager::class.java.getDeclaredField("tokenMonitorTask")
            taskField.isAccessible = true
            taskField.set(manager, mockTask)

            manager.close()

            verify { mockTask.cancel() }
        }

        @Test
        @DisplayName("should null out token monitor task after close")
        fun nullsTokenMonitorTask() {
            val mockTask = mockk<BukkitTask>(relaxed = true)

            val taskField = TwitchClientManager::class.java.getDeclaredField("tokenMonitorTask")
            taskField.isAccessible = true
            taskField.set(manager, mockTask)

            manager.close()

            assertNull(taskField.get(manager))
        }

        @Test
        @DisplayName("should clear credential after close")
        fun clearsCredential() {
            val credField = TwitchClientManager::class.java.getDeclaredField("currentCredential")
            credField.isAccessible = true
            credField.set(manager, com.github.philippheuer.credentialmanager.domain.OAuth2Credential("twitch", "test-token"))

            manager.close()

            assertNull(manager.getCredential())
        }

        @Test
        @DisplayName("should close twitchClient if initialized")
        fun closesTwitchClientIfInitialized() {
            val mockClient = mockk<com.github.twitch4j.TwitchClient>(relaxed = true)

            val clientField = TwitchClientManager::class.java.getDeclaredField("twitchClient")
            clientField.isAccessible = true
            clientField.set(manager, mockClient)

            manager.close()

            verify { mockClient.close() }
        }

        @Test
        @DisplayName("should be safe to call multiple times")
        fun safeToCallMultipleTimes() {
            assertDoesNotThrow {
                manager.close()
                manager.close()
            }
        }
    }

    // ==================== GetCredential ====================

    @Nested
    @DisplayName("GetCredential")
    inner class GetCredentialTests {

        @Test
        @DisplayName("should return null before init")
        fun returnsNullBeforeInit() {
            assertNull(manager.getCredential())
        }

        @Test
        @DisplayName("should return null after failed init")
        fun returnsNullAfterFailedInit() {
            every { twitchConfigManager.hasRequiredConfig() } returns false

            manager.init()

            assertNull(manager.getCredential())
        }
    }
}
