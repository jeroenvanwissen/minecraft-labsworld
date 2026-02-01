package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.TwitchChatAuth
import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

object AggroSubcommand : LwSubcommand {
    override val name = "aggro"

    override fun handle(ctx: CommandContext, inv: CommandInvocation) {
        if (!TwitchChatAuth.isBroadcasterOrModerator(inv.event)) {
            return inv.replyMention("You don't have permission.")
        }

        val plugin = ctx.labsWorld() ?: return inv.replyMention("Plugin error")

        plugin.server.scheduler.runTask(plugin, Runnable {
            val target = plugin.pickTargetPlayer(inv.args.getOrNull(1), allowRandom = false)
            if (target == null) {
                inv.replyMention("Usage: !lw aggro <minecraftPlayer>")
                return@Runnable
            }

            plugin.startAggroAllNpcs(target, 30)
                .onSuccess { count ->
                    if (count <= 0) inv.replyMention("No Twitch NPCs found.")
                    else inv.replyMention("Sent $count NPC(s) after ${target.name} for 30s.")
                }
                .onFailure { inv.replyMention("Aggro failed: ${it.message}") }
        })
    }
}
