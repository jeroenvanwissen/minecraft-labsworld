package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.TwitchClient
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import org.bukkit.plugin.java.JavaPlugin

enum class Permission {
    BROADCASTER,
    MODERATOR,
    VIP,
    SUBSCRIBER,
    EVERYONE,
}

enum class CommandType {
    COMMAND,
    EVENT,
    MESSAGE,
}

/** Context passed to commands with access to plugin and Twitch client. */
data class CommandContext(
    val plugin: JavaPlugin,
    val twitchClient: TwitchClient,
    val twitchConfigManager: TwitchConfigManager,
)

interface Command<T : Any> {
    val name: String
    val permission: Permission
    val type: CommandType
    var storage: T
    val twitchClient: TwitchClient

    /**
     * If true, the callback will be invoked on the Bukkit main thread.
     * Use this for any command that touches Bukkit/Paper APIs (world/entity/player/etc).
     */
    val runOnMainThread: Boolean
        get() = false

    fun init()

    fun handle(invocation: CommandInvocation)
}
