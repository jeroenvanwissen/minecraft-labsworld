package nl.jeroenlabs.labsWorld.twitch.redeems

import nl.jeroenlabs.labsWorld.twitch.TwitchContext

interface RedeemHandler {
    /** Unique, config-facing handler key, e.g. "npc.spawn". */
    val key: String

    /** Whether Bukkit work should run on the main thread. */
    val runOnMainThread: Boolean get() = true

    fun handle(context: TwitchContext, invocation: RedeemInvocation, params: Map<String, Any?>)
}
