package nl.jeroenlabs.labsWorld.twitch.actions.handlers

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation

class VillagerNpcSpawnActionHandler : ActionHandler {
    override val type: String = "npc.spawn"

    override fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val plugin = context.plugin
        val spawnPoint = plugin.pickNpcSpawnPointSpawnLocation()
            ?: error("No NPC Spawn Point placed. Ask an admin to place one.")

        plugin.ensureNpcAtSpawnPoint(invocation.userId, invocation.userName, spawnPoint)
            .onSuccess { msg -> context.twitchClient.chat.sendMessage(invocation.channelName, msg) }
            .onFailure { err -> error("NPC spawn failed: ${err.message}") }
    }
}
