package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

object DuelSubcommand : LwSubcommand {
    override val name = "duel"

    override fun handle(ctx: CommandContext, inv: CommandInvocation) {
        val plugin = ctx.labsWorld() ?: return inv.replyMention("Plugin error")

        // Require invoker to have an NPC
        if (plugin.getStoredLinkedUserName(inv.userId) == null) {
            return inv.replyMention("You don't have an NPC yet. Run !lw spawn first.")
        }

        val rawTarget = inv.args.getOrNull(1)
        if (rawTarget.isNullOrBlank()) {
            return inv.replyMention("Usage: !lw duel @TwitchUser")
        }

        val targetName = sanitizeTwitchName(rawTarget)
        if (targetName.isBlank() || targetName.equals(inv.userName, ignoreCase = true)) {
            return inv.replyMention("Invalid target. Use: !lw duel @TwitchUser")
        }

        val targetUserId = plugin.resolveLinkedUserIdByUserName(targetName)
        if (targetUserId == null) {
            return inv.replyMention("No NPC found for @$targetName. They need to run !lw spawn first.")
        }

        val targetStoredName = plugin.getStoredLinkedUserName(targetUserId) ?: targetName

        plugin.server.scheduler.runTask(plugin, Runnable {
            plugin.startNpcDuel(inv.userId, inv.userName, targetUserId, targetStoredName) { msg ->
                inv.reply(msg)
            }.onFailure { inv.replyMention("Duel failed: ${it.message}") }
        })
    }
}
