package nl.jeroenlabs.labsWorld.twitch.actions.handlers

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.ActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.ActionInvocation
import nl.jeroenlabs.labsWorld.twitch.actions.ActionUtils
import nl.jeroenlabs.labsWorld.util.anyToInt

class VillagerNpcSwarmActionHandler : ActionHandler {
    override val type: String = "npc.swarm_player"

    override fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>) {
        val plugin = context.plugin
        val target = ActionUtils.resolveTargetPlayer(invocation, params) ?: return
        val durationSeconds = anyToInt(params["duration_seconds"], 30).coerceAtLeast(1)
        plugin.startAggroAllNpcs(target, durationSeconds)
            .onSuccess { count ->
                context.twitchClient.chat.sendMessage(
                    invocation.channelName,
                    if (count <= 0) "No Twitch NPCs found." else "Sent $count NPC(s) after ${target.name} for ${durationSeconds}s.",
                )
            }
            .onFailure { err ->
                error("NPC swarm failed: ${err.message}")
            }
    }
}
