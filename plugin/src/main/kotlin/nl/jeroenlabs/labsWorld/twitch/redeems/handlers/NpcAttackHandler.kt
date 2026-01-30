package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.npc.attackLinkedNpcs
import nl.jeroenlabs.labsWorld.npc.pickTargetPlayer
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandlerContext
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation
import nl.jeroenlabs.labsWorld.twitch.redeems.anyToDouble
import nl.jeroenlabs.labsWorld.twitch.redeems.anyToInt
import nl.jeroenlabs.labsWorld.twitch.redeems.anyToString
import nl.jeroenlabs.labsWorld.twitch.redeems.pluginAsLabsWorld

class NpcAttack : RedeemHandler {
    override val key: String = "npc.attack"
    override val runOnMainThread: Boolean = true

    override fun handle(context: RedeemHandlerContext, invocation: RedeemInvocation, params: Map<String, Any?>) {
        val lw = pluginAsLabsWorld(context.plugin) ?: error("This handler requires LabsWorld plugin")

        val seconds = anyToInt(params["seconds"], 30).coerceIn(1, 300)
        val hearts = anyToDouble(params["hearts_per_hit"], 1.0).coerceIn(0.5, 10.0)
        val targetName = anyToString(params["target_player"])
        val target = pickTargetPlayer(lw, targetName, allowRandom = true) ?: error("No online Minecraft players")

        attackLinkedNpcs(lw, target, seconds, hearts).getOrThrow()
    }
}
