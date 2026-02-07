package nl.jeroenlabs.labsWorld.twitch.actions

import nl.jeroenlabs.labsWorld.twitch.TwitchContext

/**
 * Interface for individual action handlers.
 * Each handler encapsulates the logic for a single action type
 * (e.g. "player.fireworks", "player.heal").
 */
interface ActionHandler {
    /** The action type identifier, e.g. "player.fireworks". */
    val type: String

    /** Execute the action with the given context, invocation, and parameters. */
    fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>)
}
