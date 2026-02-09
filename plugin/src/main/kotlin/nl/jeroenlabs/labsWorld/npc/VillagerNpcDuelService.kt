package nl.jeroenlabs.labsWorld.npc

import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Villager
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import kotlin.random.Random

/**
 * Manages NPC duels between Twitch-linked villagers.
 *
 * Duel parameters (hit chance, speed, attack range, max HP, respawn delay)
 * are read from `twitch.config.yml` at the start of each duel so that
 * config changes take effect without a server restart.
 */
class VillagerNpcDuelService(
    private val plugin: JavaPlugin,
    private val npcLinkManager: VillagerNpcLinkManager,
    private val npcSpawnPointManager: VillagerNpcSpawnPointManager,
    private val configManager: TwitchConfigManager,
) {
    private var duelTask: BukkitTask? = null

    val isActive: Boolean
        get() = duelTask?.let { !it.isCancelled } ?: false

    /**
     * Starts a duel between two Twitch-linked NPCs.
     *
     * @param userAId Twitch user ID for the first combatant
     * @param userAName Display name for the first combatant
     * @param userBId Twitch user ID for the second combatant
     * @param userBName Display name for the second combatant
     * @param announce Callback to announce duel events (e.g., to Twitch chat)
     * @return Success if duel started, failure with reason otherwise
     */
    fun startDuel(
        userAId: String,
        userAName: String,
        userBId: String,
        userBName: String,
        announce: (String) -> Unit,
    ): Result<Unit> {
        if (userAId == userBId) return Result.failure(IllegalArgumentException("Cannot duel same user"))

        if (isActive) return Result.failure(IllegalStateException("A duel is already in progress"))

        val baseSpawn = pickSpawnLocation()
            ?: return Result.failure(IllegalStateException("No NPC Spawn Point is placed"))

        // Spawn/teleport both NPCs near each other.
        val spawnA = baseSpawn.clone().add(-1.0, 0.0, 0.0)
        val spawnB = baseSpawn.clone().add(1.0, 0.0, 0.0)

        ensureNpcAtLocation(userAId, userAName, spawnA)
        ensureNpcAtLocation(userBId, userBName, spawnB)

        val npcA = npcLinkManager.findLoadedNpcByUserId(userAId)
        val npcB = npcLinkManager.findLoadedNpcByUserId(userBId)
        if (npcA == null || npcB == null) {
            return Result.failure(IllegalStateException("Could not load both NPCs"))
        }

        // Read duel parameters from config (re-read each duel so changes take effect).
        val duelConfig = configManager.getDuelConfig()
        var hpA = duelConfig.maxHp
        var hpB = duelConfig.maxHp
        val hitChance = duelConfig.hitChance
        val speed = duelConfig.speed
        val attackRange = duelConfig.attackRange
        val attackRangeSq = attackRange * attackRange
        val damageAmount = 2.0 // 1 heart (visual feedback)

        fun label(name: String) = "@${name}".trim()

        announce("${label(userAName)} vs ${label(userBName)} — duel begins!")

        // Make sure they can actually take damage during the duel.
        npcA.isInvulnerable = false
        npcB.isInvulnerable = false
        npcA.health = npcA.getAttribute(Attribute.MAX_HEALTH)!!.value
        npcB.health = npcB.getAttribute(Attribute.MAX_HEALTH)!!.value

        val task = plugin.server.scheduler.runTaskTimer(
            plugin,
            Runnable {
                if (!npcA.isValid || !npcB.isValid) {
                    duelTask?.cancel()
                    duelTask = null
                    return@Runnable
                }

                // Keep them near the duel spot.
                if (npcA.location.world?.uid != baseSpawn.world?.uid) npcA.teleport(spawnA)
                if (npcB.location.world?.uid != baseSpawn.world?.uid) npcB.teleport(spawnB)

                // Keep them facing/closing on each other so it looks like a fight.
                npcA.setAI(true)
                npcB.setAI(true)
                npcA.removeWhenFarAway = false
                npcB.removeWhenFarAway = false
                npcA.pathfinder.moveTo(npcB.location, speed)
                npcB.pathfinder.moveTo(npcA.location, speed)

                val aAttacks = Random.nextBoolean()
                val hit = Random.nextDouble() < hitChance

                if (aAttacks) {
                    val distSq = npcA.location.distanceSquared(npcB.location)
                    if (distSq <= attackRangeSq) {
                        npcA.swingMainHand()
                        if (hit) {
                            npcB.noDamageTicks = 0
                            npcB.damage(damageAmount, npcA)
                            hpB -= 1
                        }
                    }
                } else {
                    val distSq = npcB.location.distanceSquared(npcA.location)
                    if (distSq <= attackRangeSq) {
                        npcB.swingMainHand()
                        if (hit) {
                            npcA.noDamageTicks = 0
                            npcA.damage(damageAmount, npcB)
                            hpA -= 1
                        }
                    }
                }

                val aDead = hpA <= 0
                val bDead = hpB <= 0
                if (!aDead && !bDead) return@Runnable

                // End duel.
                duelTask?.cancel()
                duelTask = null

                val winnerId: String
                val winnerName: String
                val loserId: String
                val loserName: String
                val loserNpc: Villager

                if (aDead) {
                    winnerId = userBId
                    winnerName = userBName
                    loserId = userAId
                    loserName = userAName
                    loserNpc = npcA
                } else {
                    winnerId = userAId
                    winnerName = userAName
                    loserId = userBId
                    loserName = userBName
                    loserNpc = npcB
                }

                announce("${label(winnerName)} wins! ${label(loserName)} is down.")

                // Restore winner invulnerability now that the duel is over.
                val winnerNpc = if (aDead) npcB else npcA
                if (winnerNpc.isValid) {
                    winnerNpc.isInvulnerable = true
                }

                // Remove the losing NPC and respawn after 10s.
                if (loserNpc.isValid) {
                    loserNpc.remove()
                }

                plugin.server.scheduler.runTaskLater(
                    plugin,
                    Runnable {
                        val respawnLoc = pickSpawnLocation() ?: baseSpawn
                        ensureNpcAtLocation(loserId, loserName, respawnLoc)
                        announce("${label(loserName)} respawned at the spawn point.")

                        // Best-effort: nudge winner back too so both are at spawn.
                        val winnerNpcFinal: Villager? = npcLinkManager.findLoadedNpcByUserId(winnerId)
                        winnerNpcFinal?.let { if (it.isValid) it.teleport(respawnLoc.clone().add(1.0, 0.0, 0.0)) }
                    },
                    20L * duelConfig.respawnDelaySeconds,
                )
            },
            0L,
            20L,
        )

        duelTask = task
        return Result.success(Unit)
    }

    /**
     * Picks a spawn location from the available NPC spawn points.
     * Returns null if no spawn points are placed.
     */
    private fun pickSpawnLocation(): Location? {
        npcSpawnPointManager.reconcileStoredSpawnPoints()

        val points = npcSpawnPointManager.getSpawnPointLocations()
        val chosen = points
            .sortedWith(
                compareBy<Location>({ it.world?.uid?.toString() ?: "" }, { it.blockX }, { it.blockY }, { it.blockZ }),
            )
            .firstOrNull() ?: return null

        // Spawn on top of the marker block.
        return chosen.clone().add(0.5, 1.0, 0.5)
    }

    /**
     * Ensures a linked NPC is at the given location, loading the chunk if needed.
     */
    private fun ensureNpcAtLocation(
        userId: String,
        userName: String,
        spawnLocation: Location,
    ): Result<String> {
        // Ensure chunk is available before spawning/teleporting.
        val world = spawnLocation.world ?: return Result.failure(IllegalStateException("Spawn location has no world"))
        val chunk = world.getChunkAt(spawnLocation)
        if (!chunk.isLoaded) {
            chunk.load(true)
        }

        val result = npcLinkManager.ensureNpcAt(userId, userName, spawnLocation)
        return if (result.spawned) {
            Result.success("Spawned your NPC at the spawn point.")
        } else {
            Result.success("Teleported your NPC to the spawn point.")
        }
    }
}
