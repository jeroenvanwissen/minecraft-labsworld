package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.npc.aggroLinkedNpcs
import nl.jeroenlabs.labsWorld.npc.pickTargetPlayer
import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.TwitchChatAuth
import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

class AggroSubcommand(
    private val context: CommandContext,
) : LwSubcommand {
    override val name: String = "aggro"

    override fun handle(invocation: CommandInvocation) {
        val userName = invocation.userName

        if (!TwitchChatAuth.isBroadcasterOrModerator(invocation.event)) {
            invocation.reply("@${userName} You don't have permission to use !lw aggro.")
            return
        }

        val plugin = context.plugin as? LabsWorld
        if (plugin == null) {
            invocation.reply("@${userName} Aggro failed: plugin type mismatch")
            return
        }

        // Aggro uses Bukkit APIs; run on main thread.
        plugin.server.scheduler.runTask(
            plugin,
            Runnable {
                val targetName = invocation.args.getOrNull(1)
                val targetPlayer = pickTargetPlayer(plugin, targetName, allowRandom = false)

                if (targetPlayer == null) {
                    invocation.reply("@${userName} Usage: !lw aggro <minecraftPlayerName> (or only 1 player must be online)")
                    return@Runnable
                }

                val result = aggroLinkedNpcs(plugin, targetPlayer, 30)
                result
                    .onSuccess { count ->
                        if (count <= 0) {
                            invocation.reply("@${userName} No Twitch NPCs found.")
                        } else {
                            invocation.reply("@${userName} Sent ${count} Twitch NPC(s) after ${targetPlayer.name} for 30s.")
                        }
                    }
                    .onFailure { err ->
                        invocation.reply("@${userName} Aggro failed: ${err.message ?: err::class.simpleName}")
                    }
            },
        )
    }
}
