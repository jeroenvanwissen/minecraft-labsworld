package nl.jeroenlabs.labsWorld.npc

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class VillagerNpcAggroService(
    private val plugin: JavaPlugin,
) {
    // Keep the legacy key for existing data compatibility.
    private val linkedUserIdKey = NamespacedKey(plugin, "npc_twitch_user_id")

    private var runningTask: BukkitTask? = null
    private val lastHitAtMsByNpcId = HashMap<UUID, Long>()

    /**
     * Makes all linked NPC villagers swarm/chase the target player for [durationSeconds].
     * Returns the number of NPCs that were instructed to chase.
     */
    fun startAggro(
        target: Player,
        durationSeconds: Int = 30,
    ): Result<Int> {
        if (durationSeconds <= 0) return Result.failure(IllegalArgumentException("durationSeconds must be > 0"))

        val npcs = findAllLinkedNpcs()
        if (npcs.isEmpty()) return Result.success(0)

        // If already running, replace the current run with a new one.
        runningTask?.cancel()
        runningTask = null
        lastHitAtMsByNpcId.clear()

        val durationTicks = 20L * durationSeconds.toLong()
        val periodTicks = 10L // re-issue pathing every 0.5s
        val speed = 1.2

        val task = plugin.server.scheduler.runTaskTimer(
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
                // Only stop if we haven't been replaced by a newer aggro run.
                if (runningTask == task) {
                    stop(npcs)
                }
            },
            durationTicks,
        )

        return Result.success(npcs.size)
    }

    /**
     * "Real attack" while keeping linked NPCs as villagers:
     * villagers chase the player and the plugin applies melee damage when in range.
     *
     * This is not vanilla hostile-mob AI, but the player will take actual damage attributed to the villager.
     */
    fun startAttack(
        target: Player,
        durationSeconds: Int = 30,
        damageHeartsPerHit: Double = 1.0,
        hitCooldownMs: Long = 900L,
    ): Result<Int> {
        if (durationSeconds <= 0) return Result.failure(IllegalArgumentException("durationSeconds must be > 0"))
        if (damageHeartsPerHit <= 0.0) return Result.failure(IllegalArgumentException("damageHeartsPerHit must be > 0"))
        if (hitCooldownMs <= 0L) return Result.failure(IllegalArgumentException("hitCooldownMs must be > 0"))

        val npcs = findAllLinkedNpcs()
        if (npcs.isEmpty()) return Result.success(0)

        // If already running, replace the current run with a new one.
        runningTask?.cancel()
        runningTask = null
        lastHitAtMsByNpcId.clear()

        val durationTicks = 20L * durationSeconds.toLong()
        val periodTicks = 2L // 0.1s for smoother chasing + hit detection
        val speed = 1.25
        val attackRange = 1.8
        val attackRangeSq = attackRange * attackRange
        val damageAmount = damageHeartsPerHit * 2.0

        val task = plugin.server.scheduler.runTaskTimer(
            plugin,
            Runnable {
                if (!target.isOnline) {
                    stop(npcs)
                    return@Runnable
                }

                val now = System.currentTimeMillis()
                for (villager in npcs) {
                    if (!villager.isValid) continue
                    if (villager.world.uid != target.world.uid) continue

                    villager.setAI(true)
                    villager.removeWhenFarAway = false
                    villager.pathfinder.moveTo(target.location, speed)

                    val distSq = villager.location.distanceSquared(target.location)
                    if (distSq <= attackRangeSq) {
                        val last = lastHitAtMsByNpcId[villager.uniqueId]
                        if (last == null || now - last >= hitCooldownMs) {
                            // Attribute the damage to the villager so it feels like the NPC is attacking.
                            target.damage(damageAmount, villager)
                            lastHitAtMsByNpcId[villager.uniqueId] = now
                        }
                    }
                }
            },
            0L,
            periodTicks,
        )

        runningTask = task

        plugin.server.scheduler.runTaskLater(
            plugin,
            Runnable {
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
        lastHitAtMsByNpcId.clear()
    }

    private fun stop(npcs: List<Villager>) {
        stop()
        for (villager in npcs) {
            if (!villager.isValid) continue
            villager.pathfinder.stopPathfinding()
        }
    }

    private fun findAllLinkedNpcs(): List<Villager> {
        return plugin.server.worlds
            .asSequence()
            .flatMap { it.livingEntities.asSequence() }
            .filterIsInstance<Villager>()
            .filter { it.persistentDataContainer.has(linkedUserIdKey, PersistentDataType.STRING) }
            .toList()
    }
}
