package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import nl.jeroenlabs.labsWorld.twitch.TwitchChatAuth
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

class CommandDispatcher(
    private val plugin: JavaPlugin,
    private val twitchClient: TwitchClient,
    private val twitchConfigManager: TwitchConfigManager,
) {
    private val commands = ConcurrentHashMap<String, Command<*>>()
    private val lastCommandAtMsByUserAndCommand = ConcurrentHashMap<String, Long>()
    private val initializedCommands = ConcurrentHashMap.newKeySet<String>()

    val context: CommandContext by lazy {
        CommandContext(plugin, twitchClient, twitchConfigManager)
    }

    fun register(command: Command<*>) {
        commands[command.name.lowercase()] = command
    }

    fun handle(event: ChannelMessageEvent) {
        val raw = event.message
        if (!raw.startsWith("!")) return

        val userId = event.user.id ?: event.user.name
        val now = System.currentTimeMillis()

        val parts = raw.substring(1).trim().split(" ").filter { it.isNotBlank() }
        val commandName = parts.firstOrNull() ?: return

        val command = commands[commandName.lowercase()] ?: return

        val cooldownMs =
            twitchConfigManager.getCommandCooldownMs(command.name)
                ?: twitchConfigManager.getDefaultCommandCooldownMs()
                ?: command.cooldownMs
        if (cooldownMs > 0) {
            val key = "${userId}:${command.name.lowercase()}"
            val last = lastCommandAtMsByUserAndCommand[key]
            if (last != null && now - last < cooldownMs) {
                return
            }
            lastCommandAtMsByUserAndCommand[key] = now
        }

        if (!isAuthorized(command.permission, event)) {
            twitchClient.chat.sendMessage(
                event.channel.name,
                "@${event.user.name} You don't have permission to use !$commandName",
            )
            return
        }

        if (initializedCommands.add(command.name.lowercase())) {
            runCatching { command.init() }
                .onFailure { plugin.logger.log(Level.WARNING, "Command init failed for '${command.name}'", it) }
        }

        val invocation = CommandInvocation(
            context = context,
            event = event,
            commandName = commandName,
            args = parts.drop(1),
        )

        val runner = Runnable {
            runCatching {
                command.handle(invocation)
            }.onFailure {
                plugin.logger.log(
                    Level.WARNING,
                    "Command failed name='$commandName' user='$userId' args='${invocation.args}'",
                    it,
                )
            }
        }

        if (command.runOnMainThread) {
            plugin.server.scheduler.runTask(plugin, runner)
        } else {
            plugin.server.scheduler.runTaskAsynchronously(plugin, runner)
        }
    }

    private fun isAuthorized(required: Permission, event: ChannelMessageEvent): Boolean {
        if (required == Permission.EVERYONE) return true

        if (TwitchChatAuth.isBroadcaster(event)) return true

        val tags = TwitchChatAuth.getIrcTags(event)
        val isModerator = tags["mod"] == "1" || (tags["badges"]?.contains("moderator/") == true)
        val isVip = tags["vip"] == "1" || (tags["badges"]?.contains("vip/") == true)
        val isSubscriber = tags["subscriber"] == "1" || (tags["badges"]?.contains("subscriber/") == true)

        return when (required) {
            Permission.BROADCASTER -> false
            Permission.MODERATOR -> isModerator
            Permission.VIP -> isModerator || isVip
            Permission.SUBSCRIBER -> isModerator || isVip || isSubscriber
            Permission.EVERYONE -> true
        }
    }
}
