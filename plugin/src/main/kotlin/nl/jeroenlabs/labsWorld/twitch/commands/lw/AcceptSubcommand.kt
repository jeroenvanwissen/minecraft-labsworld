package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

object AcceptSubcommand : LwSubcommand {
    override val name = "accept"

    override fun handle(ctx: TwitchContext, inv: CommandInvocation) {
        val plugin = ctx.labsWorld()

        val challenge = plugin.acceptNpcDuelChallenge(inv.userId).getOrElse {
            return inv.replyMention(it.message ?: "Could not accept challenge.")
        }

        plugin.startNpcDuel(
            challenge.challengerId,
            challenge.challengerName,
            challenge.challengedId,
            challenge.challengedName,
            challenge.announce,
        ).onFailure { inv.replyMention("Duel failed: ${it.message}") }
    }
}
