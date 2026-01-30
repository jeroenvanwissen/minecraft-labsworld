package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.npc.startNpcDuel
import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

class DuelSubcommand(
    private val context: CommandContext,
) : LwSubcommand {
    override val name: String = "duel"

    override fun handle(invocation: CommandInvocation) {
        val plugin = context.plugin as? LabsWorld
        if (plugin == null) {
            invocation.replyMention("Duel failed: plugin type mismatch")
            return
        }

        // Require the invoker to already have an NPC linked.
        val invokerStoredName = plugin.getStoredLinkedUserName(invocation.userId)
        if (invokerStoredName == null) {
            invocation.replyMention("You don't have an NPC yet. Run !lw spawn first.")
            return
        }

        val rawTarget = invocation.args.getOrNull(1)
        if (rawTarget.isNullOrBlank()) {
            invocation.replyMention("Usage: !lw duel @TwitchUser")
            return
        }

        val targetName = sanitizeTwitchName(rawTarget)
        if (targetName.isNullOrBlank()) {
            invocation.replyMention("Usage: !lw duel @TwitchUser")
            return
        }

        if (targetName.equals(invocation.userName, ignoreCase = true)) {
            invocation.replyMention("You can't duel yourself.")
            return
        }

        val targetUserId = plugin.resolveLinkedUserIdByUserName(targetName)
        if (targetUserId == null) {
            invocation.replyMention("No NPC found for @${targetName}. Ask them to run !lw spawn first.")
            return
        }

        val targetStoredName = plugin.getStoredLinkedUserName(targetUserId) ?: targetName

        // All NPC/entity operations must happen on the Bukkit main thread.
        plugin.server.scheduler.runTask(
            plugin,
            Runnable {
                val result = startNpcDuel(
                    plugin = plugin,
                    userAId = invocation.userId,
                    userAName = invocation.userName,
                    userBId = targetUserId,
                    userBName = targetStoredName,
                    announce = { msg -> invocation.reply(msg) },
                )

                result
                    .onSuccess {
                        // Duel start/end messages are announced by the duel engine.
                    }
                    .onFailure { err ->
                        invocation.replyMention("Duel failed: ${err.message ?: err::class.simpleName}")
                    }
            },
        )
    }

    private fun sanitizeTwitchName(raw: String): String {
        val trimmed = raw.trim()
        val noAt = if (trimmed.startsWith("@")) trimmed.drop(1) else trimmed
        // Twitch usernames are typically [A-Za-z0-9_]. Keep it permissive but safe.
        return noAt.takeWhile { it.isLetterOrDigit() || it == '_' }
    }
}
