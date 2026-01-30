package nl.jeroenlabs.labsWorld.twitch

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TwitchConfigManager(
    private val plugin: JavaPlugin,
) {
    private lateinit var configFile: File
    private lateinit var configYaml: YamlConfiguration

    data class TwitchConfig(
        val clientId: String?,
        val clientSecret: String?,
        val channelName: String?,
        val accessToken: String?,
        val refreshToken: String?,
    )

    data class RedeemBindingConfig(
        val rewardId: String?,
        val rewardTitle: String?,
        val handler: String,
        val cooldownMs: Long?,
        val runOnMainThread: Boolean?,
        val params: Map<String, Any?>,
    )

    private fun env(name: String): String? =
        System.getenv(name)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    fun init() {
        // Make sure the data folder exists
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        // Load configuration
        configFile = File(plugin.dataFolder, "twitch.config.yml")
        if (!configFile.exists()) {
            plugin.saveResource("twitch.config.yml", false)
        }
        configYaml = YamlConfiguration.loadConfiguration(configFile)

        if (
            configYaml.contains("client_id") ||
            configYaml.contains("client_secret") ||
            configYaml.contains("refresh_token") ||
            configYaml.contains("access_token")
        ) {
            plugin.logger.warning(
                "twitch.config.yml contains legacy Twitch auth keys. Auth is now read from environment variables (TWITCH_CLIENT_ID/TWITCH_CLIENT_SECRET/TWITCH_REFRESH_TOKEN[/TWITCH_ACCESS_TOKEN]). These YAML keys are ignored.",
            )
        }
    }

    fun reloadConfig() {
        plugin.logger.info("Reloading twitch config ${configFile}")
        configYaml = YamlConfiguration.loadConfiguration(configFile)
        plugin.logger.info("Twitch configuration reloaded")
    }

    fun getConfig(): TwitchConfig {
        val clientId = env("TWITCH_CLIENT_ID")
        val clientSecret = env("TWITCH_CLIENT_SECRET")
        val refreshToken = env("TWITCH_REFRESH_TOKEN")
        val accessToken = env("TWITCH_ACCESS_TOKEN")

        val channelName =
            configYaml.getString("channel_name")?.trim().takeIf { !it.isNullOrEmpty() }
                ?: env("TWITCH_CHANNEL_NAME")

        return TwitchConfig(
            clientId = clientId,
            clientSecret = clientSecret,
            channelName = channelName,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    fun saveConfig() {
        configYaml.save(configFile)
    }

    fun getDefaultCommandCooldownMs(): Long? {
        val section = configYaml.getConfigurationSection("commands") ?: return null
        return if (section.contains("default_cooldown_ms")) {
            section.getLong("default_cooldown_ms")
        } else {
            null
        }
    }

    fun getCommandCooldownMs(commandName: String): Long? {
        val section = configYaml.getConfigurationSection("commands") ?: return null
        val cooldowns = section.getConfigurationSection("cooldown_ms") ?: return null
        if (cooldowns.contains(commandName)) return cooldowns.getLong(commandName)
        val lowered = commandName.lowercase()
        return if (cooldowns.contains(lowered)) cooldowns.getLong(lowered) else null
    }

    fun getCommandCooldownOverridesMs(): Map<String, Long> {
        val section = configYaml.getConfigurationSection("commands") ?: return emptyMap()
        val cooldowns = section.getConfigurationSection("cooldown_ms") ?: return emptyMap()
        return cooldowns.getKeys(false).associateWith { key -> cooldowns.getLong(key) }
    }

    fun isRedeemsEnabled(): Boolean = configYaml.getBoolean("redeems.enabled", false)

    fun shouldLogUnmatchedRedeems(): Boolean = configYaml.getBoolean("redeems.log_unmatched", true)

    fun getRedeemBindings(): List<RedeemBindingConfig> {
        val section = configYaml.getConfigurationSection("redeems") ?: return emptyList()
        val list = section.getMapList("bindings")
        if (list.isEmpty()) return emptyList()

        fun anyToString(value: Any?): String? =
            when (value) {
                null -> null
                is String -> value.trim().takeIf { it.isNotEmpty() }
                else -> value.toString().trim().takeIf { it.isNotEmpty() }
            }

        fun anyToLong(value: Any?): Long? =
            when (value) {
                is Number -> value.toLong()
                is String -> value.trim().toLongOrNull()
                else -> null
            }

        fun anyToBool(value: Any?): Boolean? =
            when (value) {
                is Boolean -> value
                is String -> value.trim().lowercase().let { s ->
                    when (s) {
                        "true", "yes", "1" -> true
                        "false", "no", "0" -> false
                        else -> null
                    }
                }
                is Number -> value.toInt() != 0
                else -> null
            }

        return list.mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            val rewardId = anyToString(map["reward_id"])
            val rewardTitle = anyToString(map["reward_title"])
            val handler = anyToString(map["handler"]) ?: return@mapNotNull null

            val cooldownMs = anyToLong(map["cooldown_ms"])?.coerceAtLeast(0)
            val runOnMainThread = anyToBool(map["run_on_main_thread"])

            val paramsRaw = map["params"] as? Map<*, *> ?: emptyMap<Any?, Any?>()
            val params =
                paramsRaw.entries
                    .mapNotNull { (k, v) -> anyToString(k)?.let { it to v } }
                    .toMap()

            // Require at least one matcher.
            if (rewardId.isNullOrEmpty() && rewardTitle.isNullOrEmpty()) return@mapNotNull null

            RedeemBindingConfig(
                rewardId = rewardId,
                rewardTitle = rewardTitle,
                handler = handler,
                cooldownMs = cooldownMs,
                runOnMainThread = runOnMainThread,
                params = params,
            )
        }
    }

    fun getTwitchEnvPresence(): Map<String, Boolean> =
        linkedMapOf(
            "TWITCH_CLIENT_ID" to (env("TWITCH_CLIENT_ID") != null),
            "TWITCH_CLIENT_SECRET" to (env("TWITCH_CLIENT_SECRET") != null),
            "TWITCH_REFRESH_TOKEN" to (env("TWITCH_REFRESH_TOKEN") != null),
            "TWITCH_ACCESS_TOKEN" to (env("TWITCH_ACCESS_TOKEN") != null),
            "TWITCH_CHANNEL_NAME" to (env("TWITCH_CHANNEL_NAME") != null),
        )

    fun hasRequiredConfig(): Boolean {
        val (clientId, clientSecret, channelName, accessToken, refreshToken) = getConfig()
        val hasSecrets = !refreshToken.isNullOrEmpty() || !accessToken.isNullOrEmpty()
        return !clientId.isNullOrEmpty() &&
            !clientSecret.isNullOrEmpty() &&
            !channelName.isNullOrEmpty() &&
            hasSecrets
    }
}
