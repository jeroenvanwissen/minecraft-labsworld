package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import nl.jeroenlabs.labsWorld.twitch.TwitchAuth
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.entity.Villager
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

class CommandDispatcher(
    val context: TwitchContext,
) {
    private val plugin get() = context.plugin
    private val twitchClient get() = context.twitchClient
    private val twitchConfigManager get() = context.twitchConfigManager

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
                        showVillagerChatBubbleWhenNearby(plugin, villager, event.message, 15.0)
                    }
                },
            )

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

    private fun showVillagerChatBubbleWhenNearby(
        plugin: nl.jeroenlabs.labsWorld.LabsWorld,
        villager: Villager,
        message: String,
        radius: Double = 5.0,
        checkIntervalTicks: Long = 1L,
        ttlTicks: Long = 20L * 6L,
    ) {
        var display: TextDisplay? = null
        var elapsed = 0L

        var task: org.bukkit.scheduler.BukkitTask? = null
        task = Bukkit.getScheduler().runTaskTimer(
            plugin,
            Runnable {
                elapsed += checkIntervalTicks
                if (!villager.isValid || elapsed >= ttlTicks) {
                    display?.remove()
                    task?.cancel()
                    return@Runnable
                }

                val hasNearbyPlayer = villager.location.world?.players?.any { player ->
                    player.location.distanceSquared(villager.location) <= radius * radius
                } == true

                if (hasNearbyPlayer) {
                    val anchor = villager.location.clone().apply {
                        y = villager.boundingBox.maxY + 0.6
                    }

                    if (display == null || !display!!.isValid) {
                        display = villager.world.spawn(
                            anchor,
                            TextDisplay::class.java,
                        ) {
                            it.text(Component.text(message, NamedTextColor.BLACK))
                            it.setBackgroundColor(Color.WHITE)
                            it.billboard = Display.Billboard.CENTER
                            it.isPersistent = false
                        }
                    } else {
                        display!!.teleport(anchor)
                    }
                } else {
                    display?.remove()
                    display = null
                }
            },
            0L,
            checkIntervalTicks,
        )
    }
}
