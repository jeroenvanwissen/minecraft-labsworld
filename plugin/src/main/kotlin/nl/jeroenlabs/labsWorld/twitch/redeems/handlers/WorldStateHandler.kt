package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandlerContext
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation
import org.bukkit.Bukkit

object WorldStateHandler : RedeemHandler {
    override val key = "world.state"
    override val runOnMainThread = true

    override fun handle(context: RedeemHandlerContext, invocation: RedeemInvocation, params: Map<String, Any?>) {
        val world = Bukkit.getWorlds().firstOrNull() ?: return
        val stateType = params["type"] as? String ?: error("Missing state type")

        when (stateType.lowercase()) {
            "day" -> world.time = 1000
            "night" -> world.time = 13000
            "clear" -> world.setStorm(false)
            "rain" -> world.setStorm(true)
            "thunder" -> { world.setStorm(true); world.isThundering = true }
            else -> error("Invalid state type: $stateType")
        }
    }
}
