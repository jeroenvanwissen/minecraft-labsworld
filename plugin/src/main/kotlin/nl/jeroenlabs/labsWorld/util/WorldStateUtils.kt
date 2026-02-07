package nl.jeroenlabs.labsWorld.util

import org.bukkit.World

/**
 * Single source of truth for weather / world-state mutations.
 *
 * Both [nl.jeroenlabs.labsWorld.twitch.actions.ActionExecutor] and
 * [nl.jeroenlabs.labsWorld.twitch.redeems.handlers.WorldStateHandler]
 * delegate here so the logic is defined exactly once.
 */
object WorldStateUtils {

    /**
     * Apply a world-state change.
     *
     * @param world        the target world
     * @param state        one of `"day"`, `"night"`, `"clear"`, `"rain"`, `"storm"` / `"thunder"`
     * @param durationTicks optional duration in ticks; when `null` the server default is used
     */
    fun setWorldState(world: World, state: String, durationTicks: Int? = null) {
        when (state.lowercase()) {
            "day" -> world.time = 1000
            "night" -> world.time = 13000
            "clear" -> {
                world.setStorm(false)
                world.isThundering = false
                if (durationTicks != null) {
                    world.weatherDuration = durationTicks
                }
            }
            "rain" -> {
                world.setStorm(true)
                world.isThundering = false
                if (durationTicks != null) {
                    world.weatherDuration = durationTicks
                }
            }
            "storm", "thunder" -> {
                world.setStorm(true)
                world.isThundering = true
                if (durationTicks != null) {
                    world.weatherDuration = durationTicks
                    world.thunderDuration = durationTicks
                }
            }
            else -> error("Unknown world state '$state'")
        }
    }
}
