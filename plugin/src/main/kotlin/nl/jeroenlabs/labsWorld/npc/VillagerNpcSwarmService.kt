package nl.jeroenlabs.labsWorld.npc

import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class VillagerNpcSwarmService(
    private val plugin: JavaPlugin,
    private val linkManager: VillagerNpcLinkManager,
) {
    private var runningTask: BukkitTask? = null

    val isActive: Boolean
        get() = runningTask?.let { !it.isCancelled } ?: false

    /**
     * Makes all linked NPC villagers swarm/chase the target player for [durationSeconds].
     * Returns the number of NPCs that were instructed to chase.
     */
    fun startSwarm(
        target: Player,
        durationSeconds: Int = 30,
    ): Result<Int> {
        if (durationSeconds <= 0) return Result.failure(IllegalArgumentException("durationSeconds must be > 0"))

        if (isActive) return Result.failure(IllegalStateException("A swarm is already in progress"))

        val npcs = linkManager.findAllLinkedVillagerNpcs()
        if (npcs.isEmpty()) return Result.success(0)

        val durationTicks = 20L * durationSeconds.toLong()
        val periodTicks = 10L // re-issue pathing every 0.5s
        val speed = 1.2

        val task =
            plugin.server.scheduler.runTaskTimer(
                plugin,
                Runnable {
                    if (!target.isOnline) {
                        stop()
                        return@Runnable
                    }

                    for (villager in npcs) {
                        if (!villager.isValid) continue
                        villager.setAI(true)
                        villager.removeWhenFarAway = false

                        // Villagers are not hostile; this just makes them chase/surround the player.
                        villager.pathfinder.moveTo(target.location, speed)
                    }
                },
                0L,
                periodTicks,
            )

        runningTask = task

        plugin.server.scheduler.runTaskLater(
            plugin,
            Runnable {
                // Only stop if we haven't been replaced by a newer swarm run.
                if (runningTask == task) {
                    stop(npcs)
                }
            },
            durationTicks,
        )

        return Result.success(npcs.size)
    }

    fun stop() {
        runningTask?.cancel()
        runningTask = null
    }

    private fun stop(npcs: List<Villager>) {
        stop()
        for (villager in npcs) {
            if (!villager.isValid) continue
            villager.pathfinder.stopPathfinding()
        }
    }
}
