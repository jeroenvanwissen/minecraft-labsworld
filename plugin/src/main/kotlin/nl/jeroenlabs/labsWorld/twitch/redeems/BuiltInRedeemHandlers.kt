package nl.jeroenlabs.labsWorld.twitch.redeems

import nl.jeroenlabs.labsWorld.twitch.redeems.handlers.NpcAggro
import nl.jeroenlabs.labsWorld.twitch.redeems.handlers.NpcAttack
import nl.jeroenlabs.labsWorld.twitch.redeems.handlers.NpcSpawn
import nl.jeroenlabs.labsWorld.twitch.redeems.handlers.PlayerAction
import nl.jeroenlabs.labsWorld.twitch.redeems.handlers.Say
import nl.jeroenlabs.labsWorld.twitch.redeems.handlers.WorldState

object BuiltInRedeemHandlers {
    fun registerAll(registry: RedeemHandlerRegistry) {
        registry.register(NpcSpawn())
        registry.register(NpcAggro())
        registry.register(NpcAttack())
        registry.register(Say())
        registry.register(WorldState())
        registry.register(PlayerAction())
    }
}
