package nl.jeroenlabs.labsWorld.twitch.actions.handlers

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import nl.jeroenlabs.labsWorld.twitch.actions.ActionUtils
import nl.jeroenlabs.labsWorld.util.anyToDouble
import org.bukkit.attribute.Attribute
import kotlin.math.min

class HealActionHandler : ActionHandler {
    override val type: String = "player.heal"

    override fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val hearts = anyToDouble(params["hearts"], -1.0)
        val healthPoints = if (hearts >= 0) hearts * 2.0 else anyToDouble(params["health"], 4.0)
        if (healthPoints <= 0.0) return
        val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        player.health = min(maxHealth, player.health + healthPoints)
    }
}
