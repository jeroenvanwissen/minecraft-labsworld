package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.TwitchAuth
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

object DuelSubcommand : LwSubcommand {
    override val name = "duel"

    override fun handle(ctx: TwitchContext, inv: CommandInvocation) {
        val duelConfig = ctx.twitchConfigManager.getDuelConfig()
        if (!TwitchAuth.isAuthorized(duelConfig.permission, inv.event)) {
            return inv.replyMention("You need to be at least a ${duelConfig.permission.name.lowercase()} to start a duel.")
        }

        val plugin = ctx.labsWorld()

        // Require invoker to have an NPC
        if (plugin.getStoredLinkedUserName(inv.userId) == null) {
            return inv.replyMention("You don't have an NPC yet. Redeem the NPC spawn channel point reward first.")
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
            return inv.replyMention("No NPC found for @$targetName. They need to redeem the NPC spawn channel point reward first.")
        }

        val targetStoredName = plugin.getStoredLinkedUserName(targetUserId) ?: targetName

        plugin.createNpcDuelChallenge(inv.userId, inv.userName, targetUserId, targetStoredName) { msg ->
            inv.reply(msg)
        }.onFailure { inv.replyMention("Duel failed: ${it.message}") }
    }
}
