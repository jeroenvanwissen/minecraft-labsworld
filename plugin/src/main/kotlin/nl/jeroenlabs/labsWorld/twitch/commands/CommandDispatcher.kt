package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import nl.jeroenlabs.labsWorld.npc.ChatBubbleService
import nl.jeroenlabs.labsWorld.twitch.TwitchAuth
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

class CommandDispatcher(
    val context: TwitchContext,
) {
    private val plugin get() = context.plugin
    private val twitchClient get() = context.twitchClient
    private val twitchConfigManager get() = context.twitchConfigManager
    private val chatBubbleService = ChatBubbleService(plugin)

    private val commands = ConcurrentHashMap<String, Command>()
    private val initializedCommands = ConcurrentHashMap.newKeySet<String>()
    private val configCommandNames = ConcurrentHashMap.newKeySet<String>()
    private var configVersionSeen: Long = -1

    fun register(command: Command) {
        commands[command.name.lowercase()] = command
    }

    fun handle(event: ChannelMessageEvent) {
        refreshConfigCommandsIfNeeded()

        val raw = event.message
        if (!raw.startsWith("!")) {
            handleChatMessage(event)
            return
        }

        val parts = raw.substring(1).trim().split(" ").filter { it.isNotBlank() }
        val commandName = parts.firstOrNull() ?: return
        val command = commands[commandName.lowercase()] ?: return

        if (!TwitchAuth.isAuthorized(command.permission, event)) {
            twitchClient.chat.sendMessage(
                event.channel.name,
                "@${event.user.name} You don't have permission to use !$commandName",
            )
            return
        }

        initCommandIfNeeded(command)
        dispatchCommand(command, event, commandName, parts.drop(1))
    }

    private fun handleChatMessage(event: ChannelMessageEvent) {
        val userName = event.user.name
        val resolvedUserId =
            plugin.resolveLinkedUserIdByUserName(userName)
                ?: event.user.id
                ?: event.user.name

        plugin.server.scheduler.runTask(
            plugin,
            Runnable {
                val villager = plugin.getNpcByUserId(resolvedUserId)
                if (villager == null) {
                    plugin.logger.info("Chat message from @$userName (no linked NPC): ${event.message}")
                } else {
                    plugin.logger.info("Chat message from @$userName (NPC='${villager.name}'): ${event.message}")
                    chatBubbleService.showBubble(villager, event.message)
                }
            },
        )
    }

    private fun initCommandIfNeeded(command: Command) {
        if (initializedCommands.add(command.name.lowercase())) {
            runCatching { command.init() }
                .onFailure { plugin.logger.log(Level.WARNING, "Command init failed for '${command.name}'", it) }
        }
    }

    private fun dispatchCommand(
        command: Command,
        event: ChannelMessageEvent,
        commandName: String,
        args: List<String>,
    ) {
        val invocation = CommandInvocation(
            context = context,
            event = event,
            commandName = commandName,
            args = args,
        )

        val runner = Runnable {
            runCatching {
                command.handle(invocation)
            }.onFailure {
                plugin.logger.log(
                    Level.WARNING,
                    "Command failed name='$commandName' user='${invocation.userId}' args='${invocation.args}'",
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



    private fun refreshConfigCommandsIfNeeded() {
        val version = twitchConfigManager.getReloadVersion()
        if (version == configVersionSeen) return
        configVersionSeen = version

        configCommandNames.forEach { commands.remove(it) }
        configCommandNames.clear()

        twitchConfigManager.getCommandBindings().forEach { binding ->
            val name = binding.name.lowercase()
            val existing = commands[name]
            if (existing != null && !configCommandNames.contains(name)) {
                plugin.logger.warning(
                    "Skipping config command '${binding.name}' because a built-in command already uses that name.",
                )
                return@forEach
            }
            commands[name] = ConfigCommand(context, binding)
            configCommandNames.add(name)
        }
    }
}
