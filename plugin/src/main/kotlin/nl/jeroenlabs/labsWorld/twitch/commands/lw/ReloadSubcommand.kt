package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.TwitchChatAuth
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

object ReloadSubcommand : LwSubcommand {
    override val name = "reloadtwitch"
    override val aliases = setOf("reload")

    override fun handle(ctx: TwitchContext, inv: CommandInvocation) {
        if (!TwitchChatAuth.isBroadcasterOrModerator(inv.event)) {
            return inv.replyMention("You don't have permission.")
        }

        val plugin = ctx.labsWorld()

        inv.replyMention("Reloading Twitch config...")

        val result = plugin.reloadTwitch()
        val message = if (result.isSuccess) "Reload complete." else "Reload failed: ${result.exceptionOrNull()?.message}"

        if (!plugin.trySendTwitchMessage(inv.channelName, "@${inv.userName} $message")) {
            runCatching { inv.replyMention(message) }
        }
    }
}
