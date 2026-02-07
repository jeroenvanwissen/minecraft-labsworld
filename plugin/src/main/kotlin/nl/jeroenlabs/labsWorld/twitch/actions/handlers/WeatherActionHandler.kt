package nl.jeroenlabs.labsWorld.twitch.actions.handlers

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import nl.jeroenlabs.labsWorld.twitch.actions.ActionUtils
import nl.jeroenlabs.labsWorld.util.WorldStateUtils
import nl.jeroenlabs.labsWorld.util.anyToInt
import nl.jeroenlabs.labsWorld.util.anyToString

class WeatherActionHandler : ActionHandler {
    override val type: String = "world.weather"

    override fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params)
        val world = player?.world ?: ActionUtils.pickDefaultWorld(context.plugin.server.worlds) ?: return
        val state = anyToString(params["state"])?.lowercase() ?: "clear"
        val durationSeconds = anyToInt(params["duration_seconds"], 60).coerceAtLeast(1)
        val ticks = durationSeconds * 20

        WorldStateUtils.setWorldState(world, state, ticks)
    }
}
