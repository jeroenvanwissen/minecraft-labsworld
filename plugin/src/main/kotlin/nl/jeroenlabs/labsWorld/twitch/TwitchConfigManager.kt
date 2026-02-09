package nl.jeroenlabs.labsWorld.twitch

import nl.jeroenlabs.labsWorld.twitch.actions.ActionConfig
import nl.jeroenlabs.labsWorld.twitch.commands.Permission
import nl.jeroenlabs.labsWorld.util.anyToBool
import nl.jeroenlabs.labsWorld.util.anyToDouble
import nl.jeroenlabs.labsWorld.util.anyToInt
import nl.jeroenlabs.labsWorld.util.anyToString
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TwitchConfigManager(
    private val plugin: JavaPlugin,
) {
    private lateinit var configFile: File
    private lateinit var configYaml: YamlConfiguration
    private var reloadVersion: Long = 0

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
        val handler: String?,
        val runOnMainThread: Boolean?,
        val params: Map<String, Any?>,
        val actions: List<ActionConfig>,
    )

    data class CommandBindingConfig(
        val name: String,
        val permission: Permission,
        val runOnMainThread: Boolean?,
        val actions: List<ActionConfig>,
    )

    private fun env(name: String): String? =
        System
            .getenv(name)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    fun init() {
        // Load configuration
        configFile = File(plugin.dataFolder, "twitch.config.yml")
        if (!configFile.exists()) {
            plugin.saveResource("twitch.config.yml", false)
        }
        configYaml = YamlConfiguration.loadConfiguration(configFile)
        reloadVersion += 1

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
        plugin.logger.info("Reloading twitch config $configFile")
        configYaml = YamlConfiguration.loadConfiguration(configFile)
        reloadVersion += 1
        plugin.logger.info("Twitch configuration reloaded")
    }

    fun getReloadVersion(): Long = reloadVersion

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

    fun isRedeemsEnabled(): Boolean = configYaml.getBoolean("redeems.enabled", false)

    fun shouldLogUnmatchedRedeems(): Boolean = configYaml.getBoolean("redeems.log_unmatched", true)

    fun getRedeemBindings(): List<RedeemBindingConfig> {
        val section = configYaml.getConfigurationSection("redeems") ?: return emptyList()
        val list = section.getMapList("bindings")
        if (list.isEmpty()) return emptyList()

        return list.mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            val rewardId = anyToString(map["reward_id"])
            val rewardTitle = anyToString(map["reward_title"])
            val handler = anyToString(map["handler"])

            val runOnMainThread = anyToBool(map["run_on_main_thread"])

            val paramsRaw = map["params"] as? Map<*, *> ?: emptyMap<Any?, Any?>()
            val params =
                paramsRaw.entries
                    .mapNotNull { (k, v) -> anyToString(k)?.let { it to v } }
                    .toMap()

            val actions = parseActionList(map["actions"])

            // Require at least one matcher.
            if (rewardId.isNullOrEmpty() && rewardTitle.isNullOrEmpty()) return@mapNotNull null
            if (handler.isNullOrEmpty() && actions.isEmpty()) return@mapNotNull null

            RedeemBindingConfig(
                rewardId = rewardId,
                rewardTitle = rewardTitle,
                handler = handler,
                runOnMainThread = runOnMainThread,
                params = params,
                actions = actions,
            )
        }
    }

    fun getCommandBindings(): List<CommandBindingConfig> {
        val section = configYaml.getConfigurationSection("commands") ?: return emptyList()
        val list = section.getMapList("bindings")
        if (list.isEmpty()) return emptyList()

        return list.mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            val name = anyToString(map["name"]) ?: return@mapNotNull null
            val permission = parsePermission(anyToString(map["permission"]))
            val runOnMainThread = anyToBool(map["run_on_main_thread"])
            val actions = parseActionList(map["actions"])
            if (actions.isEmpty()) return@mapNotNull null

            CommandBindingConfig(
                name = name,
                permission = permission,
                runOnMainThread = runOnMainThread,
                actions = actions,
            )
        }
    }

    private fun parsePermission(value: String?): Permission {
        val normalized = value?.trim()?.lowercase() ?: return Permission.EVERYONE
        return when (normalized) {
            "broadcaster" -> Permission.BROADCASTER
            "moderator", "mod" -> Permission.MODERATOR
            "vip" -> Permission.VIP
            "subscriber", "sub" -> Permission.SUBSCRIBER
            else -> Permission.EVERYONE
        }
    }

    private fun parseActionList(raw: Any?): List<ActionConfig> {
        val list = raw as? List<*> ?: return emptyList()
        return list.mapNotNull { entry ->
            val map = entry as? Map<*, *> ?: return@mapNotNull null
            val type = anyToString(map["type"]) ?: return@mapNotNull null
            val paramsRaw = map["params"] as? Map<*, *> ?: emptyMap<Any?, Any?>()
            val params =
                paramsRaw.entries
                    .mapNotNull { (k, v) -> anyToString(k)?.let { it to v } }
                    .toMap()
            ActionConfig(type = type, params = params)
        }
    }

    data class DuelConfig(
        val hitChance: Double,
        val speed: Double,
        val attackRange: Double,
        val maxHp: Int,
        val respawnDelaySeconds: Long,
        val permission: Permission,
    )

    fun getDuelConfig(): DuelConfig {
        val section = configYaml.getConfigurationSection("duel")
        return DuelConfig(
            hitChance = anyToDouble(section?.get("hit_chance"), 0.65),
            speed = anyToDouble(section?.get("speed"), 1.15),
            attackRange = anyToDouble(section?.get("attack_range"), 1.9),
            maxHp = anyToInt(section?.get("max_hp"), 10),
            respawnDelaySeconds = anyToInt(section?.get("respawn_delay_seconds"), 10).toLong(),
            permission = parsePermission(anyToString(section?.get("permission")) ?: "subscriber"),
        )
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
