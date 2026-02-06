package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandlerContext
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation
import nl.jeroenlabs.labsWorld.util.anyToString
import org.bukkit.Bukkit
import org.bukkit.entity.TNTPrimed

object PlayerActionHandler : RedeemHandler {
    override val key = "player.action"
    override val runOnMainThread = true

    override fun handle(context: RedeemHandlerContext, invocation: RedeemInvocation, params: Map<String, Any?>) {
        val targetName = anyToString(params["target_player"])
        val player = if (targetName != null) Bukkit.getPlayerExact(targetName) else Bukkit.getOnlinePlayers().firstOrNull()
        player ?: return

        val actionType = params["action"] as? String ?: error("Missing action type")
        when (actionType.lowercase()) {
            "spawn_tnt" -> {
                val tnt = player.world.spawn(player.location, TNTPrimed::class.java)
                tnt.fuseTicks = 80
            }
            else -> error("Unknown action: $actionType")
        }
    }
}
