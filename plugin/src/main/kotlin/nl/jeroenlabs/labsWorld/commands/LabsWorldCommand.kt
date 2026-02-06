package nl.jeroenlabs.labsWorld.commands

import nl.jeroenlabs.labsWorld.LabsWorld
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LabsWorldCommand(
    private val plugin: LabsWorld,
) {
    fun handle(
        sender: CommandSender,
        label: String,
        args: Array<out String>,
    ): Boolean {
        val sub = args.firstOrNull()?.lowercase()
        if (sub == null) {
            sender.sendMessage("Usage: /$label <reloadtwitch|reloadconfig|status|spawnpoint>")
            return true
        }

        return when (sub) {
            "reloadtwitch" -> {
                sender.sendMessage("Reloading Twitch config and reconnecting...")
                plugin.server.scheduler.runTaskAsynchronously(
                    plugin,
                    Runnable {
                        val result = plugin.reloadTwitch()
                        plugin.server.scheduler.runTask(
                            plugin,
                            Runnable {
                                result
                                    .onSuccess { sender.sendMessage("Reload complete.") }
                                    .onFailure { sender.sendMessage("Reload failed: ${it.message ?: it::class.simpleName}") }
                            },
                        )
                    },
                )
                true
            }

            "reloadconfig" -> {
                val result = plugin.reloadConfigOnly()
                result
                    .onSuccess { sender.sendMessage("Reloaded twitch.config.yml") }
                    .onFailure { sender.sendMessage("Reload failed: ${it.message ?: it::class.simpleName}") }
                true
            }

            "status" -> {
                val config = plugin.twitchStatusSnapshot()
                sender.sendMessage("LabsWorld Twitch status:")
                sender.sendMessage("- connected: ${config.connected}")
                sender.sendMessage("- channel: ${config.channelName ?: "(missing)"}")
                sender.sendMessage("- has_required_config: ${config.hasRequiredConfig}")
                sender.sendMessage("- env: ${config.envPresence.entries.joinToString(", ") { (k, v) -> "$k=${if (v) "set" else "missing"}" }}")
                sender.sendMessage("- npc_spawn_points: ${plugin.npcSpawnPointCount()}")
                true
            }

            "spawnpoint" -> {
                if (sender !is Player) {
                    sender.sendMessage("This command can only be used in-game.")
                    return true
                }

                val item = plugin.createNpcSpawnPointItem(1)
                val leftover = sender.inventory.addItem(item)
                if (leftover.isNotEmpty()) {
                    sender.world.dropItemNaturally(sender.location, item)
                    sender.sendMessage("Inventory full; dropped NPC Spawn Point at your feet.")
                } else {
                    sender.sendMessage("Given: NPC Spawn Point")
                }

                true
            }

            else -> {
                sender.sendMessage("Unknown subcommand '$sub'. Try: /$label <reloadtwitch|reloadconfig|status|spawnpoint>")
                true
            }
        }
    }
}
