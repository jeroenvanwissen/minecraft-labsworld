package nl.jeroenlabs.labsWorld.twitch

import com.github.philippheuer.credentialmanager.CredentialManager
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.auth.providers.TwitchIdentityProvider
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class TwitchClientManager(
    private val plugin: JavaPlugin,
    private val twitchConfigManager: TwitchConfigManager,
) {
    private lateinit var twitchClient: TwitchClient
    private lateinit var credentialManager: CredentialManager
    private lateinit var identityProvider: TwitchIdentityProvider
    private var tokenMonitorTask: BukkitTask? = null
    private var currentCredential: OAuth2Credential? = null

    fun init(): Boolean {
        if (!twitchConfigManager.hasRequiredConfig()) {
            plugin.logger.severe(
                "Missing Twitch configuration! Please configure your Twitch credentials in twitch.config.yml",
            )
            return false
        }

        val connected = connect()
        if (connected) {
            tokenMonitoring()
        }
        return connected
    }

    private fun connect(): Boolean {
        val config = twitchConfigManager.getConfig()

        val clientId = config.clientId
        val clientSecret = config.clientSecret
        val channelName = config.channelName
        val accessToken = config.accessToken
        val refreshToken = config.refreshToken

        if (clientId.isNullOrEmpty() || clientSecret.isNullOrEmpty() || channelName.isNullOrEmpty()) {
            plugin.logger.severe("Missing required Twitch configuration (client id/secret/channel).")
            return false
        }

        // Initialize credential manager
        credentialManager = CredentialManagerBuilder.builder().build()

        // Setup identity provider for auto token refresh
        identityProvider = TwitchIdentityProvider(clientId, clientSecret, "")
        credentialManager.registerIdentityProvider(identityProvider)

        // Create OAuth credential and validate/refresh if needed
        var credential = OAuth2Credential("twitch", accessToken ?: "")
        if (!refreshToken.isNullOrEmpty()) {
            credential.refreshToken = refreshToken
        }

        if (credential.accessToken.isEmpty() || !isCredentialValid(credential.accessToken)) {
            if (refreshToken.isNullOrEmpty()) {
                plugin.logger.warning(
                    "No valid access token and no refresh token available. Set TWITCH_REFRESH_TOKEN (recommended) or TWITCH_ACCESS_TOKEN.",
                )
                return false
            }
            try {
                plugin.logger.info("Generating new access token using refresh token...")
                val refreshResult = identityProvider.refreshCredential(credential)
                if (refreshResult.isPresent) {
                    val refreshedCredential = refreshResult.get()
                    credential = refreshedCredential
                    credential.refreshToken = refreshToken
                    plugin.logger.info("Successfully generated new access token from refresh token")
                } else {
                    plugin.logger.severe(
                        "Failed to generate access token: No credential returned from refresh",
                    )
                    return false
                }
            } catch (e: Exception) {
                plugin.logger.severe("Failed to generate access token: ${e.message}")
                return false
            }
        }

        currentCredential = credential

        // Set up the TwitchClient
        twitchClient =
            TwitchClientBuilder
                .builder()
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withCredentialManager(credentialManager)
                .withDefaultAuthToken(credential)
                .withChatAccount(credential)
                .withEnableChat(true)
                .withEnableHelix(true)
                .withEnableEventSocket(true)
                .build()

        // Join the channel's chat
        twitchClient.chat.joinChannel(channelName)

        plugin.logger.info("Successfully connected to Twitch!")
        return true
    }

    private fun tokenMonitoring() {
        // Cancel any existing task
        tokenMonitorTask?.cancel()

        // Start new task that runs every 30 minutes (36000 ticks)
        tokenMonitorTask =
            Bukkit
                .getScheduler()
                .runTaskTimerAsynchronously(
                    plugin,
                    Runnable {
                        try {
                            val token = currentCredential?.accessToken
                            if (!isCredentialValid(token)) {
                                connect()
                            }
                        } catch (e: Exception) {
                            plugin.logger.severe(
                                "Failed to refresh twitch access token and reconnect: ${e.message}",
                            )
                        }
                    },
                    36000L,
                    36000L,
                )
    }

    private fun isCredentialValid(accessToken: String?): Boolean {
        if (accessToken.isNullOrEmpty() || accessToken.length <= 10) {
            return false
        }

        val config = twitchConfigManager.getConfig()
        val clientId = config.clientId
        val clientSecret = config.clientSecret
        val channelName = config.channelName

        if (clientId.isNullOrEmpty() || clientSecret.isNullOrEmpty() || channelName.isNullOrEmpty()) {
            return false
        }

        try {
            val tempTwitchClient =
                TwitchClientBuilder
                    .builder()
                    .withClientId(clientId)
                    .withClientSecret(clientSecret)
                    .withEnableHelix(true)
                    .withDefaultAuthToken(OAuth2Credential("twitch", accessToken))
                    .build()

            val result = tempTwitchClient.helix.getUsers(null, null, listOf(channelName)).execute()
            tempTwitchClient.close()

            return result.users.isNotEmpty()
        } catch (e: Exception) {
            plugin.logger.warning("Access token validation failed: ${e.message}")
            return false
        }
    }

    fun close() {
        tokenMonitorTask?.cancel()
        tokenMonitorTask = null

        currentCredential = null

        if (::twitchClient.isInitialized) {
            twitchClient.close()
        }
    }

    fun getTwitchClient(): TwitchClient = twitchClient

    fun getCredential(): OAuth2Credential? = currentCredential

    fun isConnected(): Boolean = ::twitchClient.isInitialized

    fun getChannelId(): String? {
        val channelName = twitchConfigManager.getConfig().channelName
        try {
            plugin.logger.info("Getting channel ID for: $channelName")
            val userList = twitchClient.helix.getUsers(null, null, listOf(channelName)).execute()
            if (userList.users.isNotEmpty()) {
                val channelId = userList.users[0].id
                plugin.logger.info("Found channel ID: $channelId for: $channelName")
                return channelId
            } else {
                return null
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error getting channel ID for: $channelName - ${e.message}")
            return null
        }
    }
}
