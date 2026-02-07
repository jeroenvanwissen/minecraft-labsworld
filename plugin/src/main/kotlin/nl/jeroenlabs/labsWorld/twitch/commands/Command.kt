package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.TwitchClient

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

interface Command {
    val name: String
    val permission: Permission
    val type: CommandType
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
