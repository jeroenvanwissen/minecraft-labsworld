package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionExecutor
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

object SpawnSubcommand : LwSubcommand {
    override val name = "spawn"

    override fun handle(ctx: TwitchContext, inv: CommandInvocation) {
        val actionInvocation = ActionInvocation.fromCommand(inv)
        runCatching {
            ActionExecutor.executeAction(ctx, actionInvocation, "npc.spawn")
        }.onFailure { err ->
            inv.replyMention("Spawn failed: ${err.message}")
        }
    }
}
