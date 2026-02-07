package nl.jeroenlabs.labsWorld.twitch.actions.handlers

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import nl.jeroenlabs.labsWorld.twitch.actions.ActionUtils
import nl.jeroenlabs.labsWorld.util.anyToDouble
import nl.jeroenlabs.labsWorld.util.anyToInt
import nl.jeroenlabs.labsWorld.util.anyToString

class SpawnMobActionHandler : ActionHandler {
    override val type: String = "player.spawn_mob"

    override fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val player = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val mobName = anyToString(params["mob"]) ?: error("Missing mob type")
        val entityType = ActionUtils.parseEntityType(mobName) ?: error("Unknown mob type '$mobName'")
        if (!entityType.isSpawnable || !entityType.isAlive) return

        val count = anyToInt(params["count"], 1).coerceAtLeast(1)
        val radius = anyToDouble(params["radius"], 2.0).coerceAtLeast(0.0)
        repeat(count) {
            val location = player.location.clone().add(ActionUtils.randomOffset(radius))
            player.world.spawnEntity(location, entityType)
        }
    }
}
