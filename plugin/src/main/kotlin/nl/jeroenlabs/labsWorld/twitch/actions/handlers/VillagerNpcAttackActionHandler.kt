package nl.jeroenlabs.labsWorld.twitch.actions.handlers

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import nl.jeroenlabs.labsWorld.twitch.actions.ActionUtils
import nl.jeroenlabs.labsWorld.util.anyToDouble
import nl.jeroenlabs.labsWorld.util.anyToInt

class VillagerNpcAttackActionHandler : ActionHandler {
    override val type: String = "npc.attack_player"

    override fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val plugin = context.plugin
        val target = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val durationSeconds = anyToInt(params["duration_seconds"], 30).coerceAtLeast(1)
        val heartsPerHit = anyToDouble(params["hearts_per_hit"], 2.0).coerceAtLeast(0.1)

        plugin.startAttackAllNpcs(target, durationSeconds, heartsPerHit)
            .onSuccess { count ->
                context.twitchClient.chat.sendMessage(
                    invocation.channelName,
                    if (count <= 0) "No Twitch NPCs found."
                    else "Sent $count NPC(s) to attack ${target.name} for ${durationSeconds}s (${heartsPerHit}❤/hit).",
                )
            }
            .onFailure { err ->
                error("NPC attack failed: ${err.message}")
            }
    }
}
