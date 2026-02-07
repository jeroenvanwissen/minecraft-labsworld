package nl.jeroenlabs.labsWorld.twitch.actions

import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.actions.handlers.DropItemsActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.handlers.FireworksActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.handlers.HealActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.handlers.LootChestActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.handlers.SpawnMobActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.handlers.VillagerNpcAttackActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.handlers.VillagerNpcSpawnActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.handlers.VillagerNpcSwarmActionHandler
import nl.jeroenlabs.labsWorld.twitch.actions.handlers.WeatherActionHandler

object ActionExecutor {
    private val handlers: Map<String, ActionHandler> = buildMap {
        val lootChest = LootChestActionHandler()
        listOf(
            FireworksActionHandler(),
            HealActionHandler(),
            SpawnMobActionHandler(),
            DropItemsActionHandler(),
            WeatherActionHandler(),
            lootChest,
            VillagerNpcSpawnActionHandler(),
            VillagerNpcSwarmActionHandler(),
            VillagerNpcAttackActionHandler(),
        ).forEach { put(it.type, it) }
        // LootChestActionHandler also handles "player.loot_chest"
        lootChest.additionalTypes.forEach { put(it, lootChest) }
    }

    fun executeActions(context: TwitchContext, invocation: ActionInvocation, actions: List<ActionConfig>) {
        actions.forEach { action ->
            runCatching {
                executeAction(context, invocation, action)
            }.onFailure { err ->
                context.plugin.logger.warning(
                    "Action failed type='${action.type}' user='${invocation.userName}' error='${err.message}'",
                )
            }
        }
    }

    private fun executeAction(context: TwitchContext, invocation: ActionInvocation, action: ActionConfig) {
        executeAction(context, invocation, action.type, action.params)
    }

    fun executeAction(context: TwitchContext, invocation: ActionInvocation, type: String, params: Map<String, Any?> = emptyMap()) {
        val handler = handlers[type.lowercase()]
            ?: error("Unknown action type '$type'")
        handler.handle(context, invocation, params)
    }
}
