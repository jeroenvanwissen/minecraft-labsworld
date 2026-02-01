package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandler

/** All registered redeem handlers */
object RedeemHandlers {
    val all: List<RedeemHandler> = listOf(
        NpcSpawnHandler,
        NpcAggroHandler,
        NpcAttackHandler,
        ChatSayHandler,
        WorldStateHandler,
        PlayerActionHandler,
    )
}
