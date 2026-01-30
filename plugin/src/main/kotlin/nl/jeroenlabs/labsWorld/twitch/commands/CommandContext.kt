package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.TwitchClient
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import org.bukkit.plugin.java.JavaPlugin

data class CommandContext(
    val plugin: JavaPlugin,
    val twitchClient: TwitchClient,
    val twitchConfigManager: TwitchConfigManager,
)
