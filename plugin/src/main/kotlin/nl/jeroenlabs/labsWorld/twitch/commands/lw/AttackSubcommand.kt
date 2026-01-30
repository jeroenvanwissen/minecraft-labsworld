package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.TwitchChatAuth
import nl.jeroenlabs.labsWorld.npc.attackLinkedNpcs
import nl.jeroenlabs.labsWorld.npc.parseDamageHearts
import nl.jeroenlabs.labsWorld.npc.parseDurationSeconds
import nl.jeroenlabs.labsWorld.npc.pickTargetPlayer
import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

class AttackSubcommand(
    private val context: CommandContext,
) : LwSubcommand {
    override val name: String = "attack"

    override fun handle(invocation: CommandInvocation) {
        val userName = invocation.userName

        if (!TwitchChatAuth.isBroadcasterOrModerator(invocation.event)) {
            invocation.reply("@${userName} You don't have permission to use !lw attack.")
            return
        }

        val plugin = context.plugin as? LabsWorld
        if (plugin == null) {
            invocation.reply("@${userName} Attack failed: plugin type mismatch")
            return
        }

        plugin.server.scheduler.runTask(
            plugin,
            Runnable {
                val targetName = invocation.args.getOrNull(1)
                val secondsArg = invocation.args.getOrNull(2)
                val heartsArg = invocation.args.getOrNull(3)

                val durationSeconds = parseDurationSeconds(secondsArg, defaultSeconds = 30)
                val damageHearts = parseDamageHearts(heartsArg, defaultHearts = 1.0)

                val targetPlayer = pickTargetPlayer(plugin, targetName, allowRandom = false)

                if (targetPlayer == null) {
                    invocation.reply(
                        "@${userName} Usage: !lw attack <minecraftPlayerName> [seconds] [heartsPerHit] (or only 1 player must be online)",
                    )
                    return@Runnable
                }

                val result = attackLinkedNpcs(plugin, targetPlayer, durationSeconds, damageHearts)
                result
                    .onSuccess { count ->
                        if (count <= 0) {
                            invocation.reply("@${userName} No Twitch NPCs found.")
                        } else {
                            invocation.reply(
                                "@${userName} ${count} Twitch NPC(s) are attacking ${targetPlayer.name} for ${durationSeconds}s (damage=${damageHearts}❤/hit).",
                            )
                        }
                    }
                    .onFailure { err ->
                        invocation.reply("@${userName} Attack failed: ${err.message ?: err::class.simpleName}")
                    }
            },
        )
    }
}
