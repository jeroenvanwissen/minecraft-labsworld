package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandlerContext
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation
import nl.jeroenlabs.labsWorld.util.PlayerUtils
import nl.jeroenlabs.labsWorld.util.anyToDouble
import nl.jeroenlabs.labsWorld.util.anyToInt
import nl.jeroenlabs.labsWorld.util.anyToString

object NpcAttackHandler : RedeemHandler {
    override val key = "npc.attack"
    override val runOnMainThread = true

    override fun handle(context: RedeemHandlerContext, invocation: RedeemInvocation, params: Map<String, Any?>) {
        val lw = pluginAsLabsWorld(context.plugin) ?: error("Requires LabsWorld plugin")

        val seconds = anyToInt(params["seconds"], 30).coerceIn(1, 300)
        val hearts = anyToDouble(params["hearts_per_hit"], 1.0).coerceIn(0.5, 10.0)
        val targetName = anyToString(params["target_player"])
        val target = PlayerUtils.pickTargetPlayer(lw.server, targetName, allowRandom = true) ?: error("No online players")

        lw.startAttackAllNpcs(target, seconds, hearts).getOrThrow()
    }
}
