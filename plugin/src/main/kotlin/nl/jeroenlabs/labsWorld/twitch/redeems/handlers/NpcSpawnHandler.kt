package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.npc.ensureLinkedNpcAtSpawnPoint
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandlerContext
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation
import nl.jeroenlabs.labsWorld.twitch.redeems.pluginAsLabsWorld
import nl.jeroenlabs.labsWorld.twitch.redeems.renderTemplate

class NpcSpawn : RedeemHandler {
    override val key: String = "npc.spawn"
    override val runOnMainThread: Boolean = true

    override fun handle(context: RedeemHandlerContext, invocation: RedeemInvocation, params: Map<String, Any?>) {
        val lw = pluginAsLabsWorld(context.plugin) ?: error("This handler requires LabsWorld plugin")

        val result = ensureLinkedNpcAtSpawnPoint(lw, invocation.userId, invocation.userName)
        val message = params["message"] as? String
        if (!message.isNullOrBlank()) {
            val rendered = renderTemplate(message, invocation)
            context.say(rendered)
        }
        result.getOrThrow()
    }
}
