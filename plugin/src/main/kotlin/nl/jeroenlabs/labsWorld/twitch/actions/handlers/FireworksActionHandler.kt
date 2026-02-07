package nl.jeroenlabs.labsWorld.twitch.actions.handlers

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.entity.Firework
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import nl.jeroenlabs.labsWorld.twitch.actions.ActionUtils
import nl.jeroenlabs.labsWorld.util.anyToInt
import nl.jeroenlabs.labsWorld.util.anyToString
import nl.jeroenlabs.labsWorld.util.anyToStringList

class FireworksActionHandler : ActionHandler {
    override val type: String = "player.fireworks"

    override fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val count = anyToInt(params["count"], 1).coerceAtLeast(1)
        val power = anyToInt(params["power"], 1).coerceIn(0, 2)
        val shape = anyToString(params["shape"])?.lowercase() ?: "ball"
        val colors = ActionUtils.parseColors(anyToStringList(params["colors"]))

        repeat(count) {
            val location = player.location.clone().add(ActionUtils.randomOffset(0.6))
            val firework = player.world.spawn(location, Firework::class.java)
            val meta = firework.fireworkMeta
            val effect = FireworkEffect.builder()
                .with(ActionUtils.parseFireworkType(shape))
                .withColor(colors.ifEmpty { listOf(Color.WHITE) })
                .build()
            meta.power = power
            meta.addEffect(effect)
            firework.fireworkMeta = meta
        }
    }
}
