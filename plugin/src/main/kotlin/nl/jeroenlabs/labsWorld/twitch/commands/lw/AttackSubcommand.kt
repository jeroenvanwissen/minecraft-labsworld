package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.TwitchChatAuth
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation
import nl.jeroenlabs.labsWorld.util.PlayerUtils

object AttackSubcommand : LwSubcommand {
    override val name = "attack"

    override fun handle(ctx: TwitchContext, inv: CommandInvocation) {
        if (!TwitchChatAuth.isBroadcasterOrModerator(inv.event)) {
            return inv.replyMention("You don't have permission.")
        }

        val plugin = ctx.labsWorld()

        plugin.server.scheduler.runTask(plugin, Runnable {
            val target = PlayerUtils.pickTargetPlayer(plugin.server, inv.args.getOrNull(1), allowRandom = false)
            if (target == null) {
                inv.replyMention("Usage: !lw attack <player> [seconds] [hearts]")
                return@Runnable
            }

            val seconds = parseDuration(inv.args.getOrNull(2))
            val hearts = parseDamage(inv.args.getOrNull(3))

            plugin.startAttackAllNpcs(target, seconds, hearts)
                .onSuccess { count ->
                    if (count <= 0) inv.replyMention("No Twitch NPCs found.")
                    else inv.replyMention("$count NPC(s) attacking ${target.name} for ${seconds}s (${hearts}❤/hit).")
                }
                .onFailure { inv.replyMention("Attack failed: ${it.message}") }
        })
    }
}
