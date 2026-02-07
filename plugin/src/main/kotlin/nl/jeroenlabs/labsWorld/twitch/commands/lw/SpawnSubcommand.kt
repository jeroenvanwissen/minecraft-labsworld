package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation

object SpawnSubcommand : LwSubcommand {
    override val name = "spawn"

    override fun handle(ctx: TwitchContext, inv: CommandInvocation) {
        val plugin = ctx.labsWorld()

        val spawnPoint = plugin.pickNpcSpawnPointSpawnLocation()
        if (spawnPoint == null) {
            inv.replyMention("No NPC Spawn Point placed. Ask an admin to place one.")
            return
        }

        plugin.ensureNpcAtSpawnPoint(inv.userId, inv.userName, spawnPoint)
            .onSuccess { msg -> inv.replyMention(msg) }
            .onFailure { inv.replyMention("Spawn failed: ${it.message}") }
    }
}
