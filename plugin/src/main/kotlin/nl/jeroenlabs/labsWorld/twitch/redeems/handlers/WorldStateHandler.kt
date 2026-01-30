package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandlerContext
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation
import nl.jeroenlabs.labsWorld.twitch.redeems.pluginAsLabsWorld
import org.bukkit.Bukkit

class WorldState: RedeemHandler {
    override val key: String = "world.state"
    override val runOnMainThread: Boolean = true

    override fun handle(context: RedeemHandlerContext, invocation: RedeemInvocation, params: Map<String, Any?>) {
        val lw = pluginAsLabsWorld(context.plugin) ?: error("This handler requires LabsWorld plugin")
        // For simplicity, we target the default world named "world" ( HARDCODED!! )
        val world = Bukkit.getWorld("world") ?: return

        val stateType = params["type"] as? String ?: error("Missing state type parameter")
        when (stateType.lowercase()) {
            "day" -> world.time = 1000
            "night" -> world.time = 13000
            "clear" -> world.setStorm(false)
            "rain" -> world.setStorm(true)
            "thunder" -> {
                world.setStorm(true)
                world.isThundering = true
            }
            else -> error("Invalid weather type: $stateType")
        }
    }
}
