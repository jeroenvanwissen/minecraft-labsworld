package nl.jeroenlabs.labsWorld.twitch.redeems

interface RedeemHandler {
    /**
     * Unique, config-facing handler key, e.g. "npc.spawn".
     */
    val key: String

    /**
     * Whether Bukkit work should run on the main thread.
     */
    val runOnMainThread: Boolean get() = true

    fun handle(context: RedeemHandlerContext, invocation: RedeemInvocation, params: Map<String, Any?>)
}
