package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler
import org.bukkit.Bukkit

class PlayerAction: RedeemHandler {
    override val key: String = "player.action"
    override val runOnMainThread: Boolean = true

    override fun handle(context: nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandlerContext, invocation: nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation, params: Map<String, Any?>) {
        val player = Bukkit.getPlayer("Jeroenster") ?: return
        val actionType = params["action"] as? String ?: error("Missing action type parameter")
        when (actionType.lowercase()) {
            "spawn_tnt" -> {
                val tnt = player.world.spawn(player.location, org.bukkit.entity.TNTPrimed::class.java)
                tnt.fuseTicks = 80 // 4 seconds
            }
            else -> error("Unknown action type: $actionType")
        }
    }
}
