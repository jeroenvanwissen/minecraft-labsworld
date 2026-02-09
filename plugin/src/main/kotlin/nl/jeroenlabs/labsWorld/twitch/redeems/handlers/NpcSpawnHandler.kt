package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.say
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation

object NpcSpawnHandler : RedeemHandler {
    override val key = "npc.spawn"
    override val runOnMainThread = true

    override fun handle(context: TwitchContext, invocation: RedeemInvocation, params: Map<String, Any?>) {
        val lw = context.plugin

        val spawnPoint = lw.pickNpcSpawnPointSpawnLocation().getOrThrow()
        lw.ensureNpcAtSpawnPoint(invocation.userId, invocation.userName, spawnPoint).getOrThrow()

        val message = params["message"] as? String
        if (!message.isNullOrBlank()) {
            context.say(renderTemplate(message, invocation))
        }
    }
}
