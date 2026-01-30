package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.TwitchChatAuth
import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

class ReloadTwitchSubcommand(
    private val context: CommandContext,
) : LwSubcommand {
    override val name: String = "reloadtwitch"
    override val aliases: Set<String> = setOf("reload")

    override fun handle(invocation: CommandInvocation) {
        val userName = invocation.userName

        if (!TwitchChatAuth.isBroadcasterOrModerator(invocation.event)) {
            invocation.reply("@${userName} You don't have permission to reload config.")
            return
        }

        // Acknowledge before reconnecting; the current Twitch client may be replaced.
        invocation.reply("@${userName} Reloading Twitch config and reconnecting...")

        val plugin = context.plugin as? LabsWorld
        if (plugin == null) {
            invocation.reply("@${userName} Reload failed: plugin type mismatch")
            return
        }

        val result = plugin.reloadTwitch()
        val done = result.isSuccess
        val message =
            if (done) {
                "@${userName} Reload complete."
            } else {
                val err = result.exceptionOrNull()
                "@${userName} Reload failed: ${err?.message ?: err?.let { it::class.simpleName } ?: "unknown"}"
            }

        if (!plugin.trySendTwitchMessage(invocation.channelName, message)) {
            // Best-effort fallback: try the original client.
            runCatching { invocation.reply(message) }
        }
    }
}
