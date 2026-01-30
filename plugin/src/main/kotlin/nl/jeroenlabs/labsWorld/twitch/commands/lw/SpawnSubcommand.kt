package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.npc.ensureLinkedNpcAtSpawnPoint
import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

class SpawnSubcommand(
    private val context: CommandContext,
) : LwSubcommand {
    override val name: String = "spawn"

    override fun handle(invocation: CommandInvocation) {
        val userName = invocation.userName
        val plugin = context.plugin as? LabsWorld
        if (plugin == null) {
            invocation.reply("@${userName} Spawn failed: plugin type mismatch")
            return
        }

        // Bukkit entity/world work must happen on the main thread.
        plugin.server.scheduler.runTask(
            plugin,
            Runnable {
                val result = ensureLinkedNpcAtSpawnPoint(plugin, invocation.userId, invocation.userName)
                result
                    .onSuccess { msg -> invocation.reply("@${userName} $msg") }
                    .onFailure { err ->
                        val msg = if (err.message == "No NPC Spawn Point is placed") {
                            "No NPC Spawn Point is placed. Ask an admin to place one first."
                        } else {
                            "Spawn failed: ${err.message ?: err::class.simpleName}"
                        }
                        invocation.reply("@${userName} $msg")
                    }
            },
        )
    }
}
