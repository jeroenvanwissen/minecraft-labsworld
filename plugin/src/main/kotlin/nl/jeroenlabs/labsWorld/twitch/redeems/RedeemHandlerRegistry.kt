package nl.jeroenlabs.labsWorld.twitch.redeems

import java.util.concurrent.ConcurrentHashMap

class RedeemHandlerRegistry {
    private val byKey = ConcurrentHashMap<String, RedeemHandler>()

    fun register(handler: RedeemHandler) {
        byKey[handler.key.lowercase()] = handler
    }

    fun get(key: String): RedeemHandler? = byKey[key.lowercase()]

    fun keys(): Set<String> = byKey.keys
}
