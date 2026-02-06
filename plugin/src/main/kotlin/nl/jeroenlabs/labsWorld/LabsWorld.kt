package nl.jeroenlabs.labsWorld

import nl.jeroenlabs.labsWorld.npc.NpcAggroService
import nl.jeroenlabs.labsWorld.npc.NpcLinkManager
import nl.jeroenlabs.labsWorld.npc.NpcManager
import nl.jeroenlabs.labsWorld.npc.NpcSpawnPointListener
import nl.jeroenlabs.labsWorld.npc.NpcSpawnPointManager
import nl.jeroenlabs.labsWorld.twitch.TwitchClientManager
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import nl.jeroenlabs.labsWorld.twitch.TwitchEventHandler
import nl.jeroenlabs.labsWorld.commands.LabsWorldPaperCommand
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class LabsWorld : JavaPlugin() {
    private lateinit var twitchConfigManager: TwitchConfigManager
    private lateinit var twitchClientManager: TwitchClientManager
    private lateinit var twitchEventHandler: TwitchEventHandler
    private lateinit var npcManager: NpcManager
    private lateinit var npcSpawnPointManager: NpcSpawnPointManager
    private lateinit var npcLinkManager: NpcLinkManager
    private lateinit var npcAggroService: NpcAggroService

    private var twitchNpcDuelTask: BukkitTask? = null

    private val twitchReloadInProgress = AtomicBoolean(false)

    override fun onEnable() {
        initializeComponents()
    }

    override fun onDisable() {
        if (::twitchClientManager.isInitialized) {
            twitchClientManager.close()
        }
    }

    private fun initializeComponents() {
        twitchConfigManager = TwitchConfigManager(this)
        twitchConfigManager.init()

        twitchClientManager = TwitchClientManager(this, twitchConfigManager)
        val twitchConnected = twitchClientManager.init()

        if (twitchConnected) {
            twitchEventHandler =
                TwitchEventHandler(
                    this,
                    twitchClientManager.getTwitchClient(),
                    twitchConfigManager,
                    twitchClientManager,
                )
            twitchEventHandler.registerEventHandlers()
        } else {
            logger.warning("Twitch is not connected; Twitch chat commands will be unavailable until config is fixed.")
        }

        npcManager = NpcManager(this)

        npcLinkManager = NpcLinkManager(this, npcManager)
        npcLinkManager.init()

        npcAggroService = NpcAggroService(this)

        npcSpawnPointManager = NpcSpawnPointManager(this)
        npcSpawnPointManager.init()
        server.pluginManager.registerEvents(NpcSpawnPointListener(npcSpawnPointManager), this)
        server.scheduler.runTask(this, Runnable { npcSpawnPointManager.reconcileStoredSpawnPoints() })

        registerCommand("labsworld", LabsWorldPaperCommand(this))
    }

    fun createNpcSpawnPointItem(amount: Int = 1) = npcSpawnPointManager.createSpawnPointItem(amount)

    fun npcSpawnPointCount(): Int = npcSpawnPointManager.getSpawnPointLocations().size

    fun pickNpcSpawnPointSpawnLocation(): Location? {
        // Ensure stored list is accurate.
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

    fun ensureNpcAtSpawnPoint(
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

    fun startAggroAllNpcs(
        target: Player,
        durationSeconds: Int = 30,
    ): Result<Int> {
        return npcAggroService.startAggro(target, durationSeconds)
    }

    fun startAttackAllNpcs(
        target: Player,
        durationSeconds: Int = 30,
        damageHeartsPerHit: Double = 1.0,
    ): Result<Int> {
        return npcAggroService.startAttack(
            target = target,
            durationSeconds = durationSeconds,
            damageHeartsPerHit = damageHeartsPerHit,
        )
    }

    fun reloadTwitch(): Result<Unit> {
        if (!twitchReloadInProgress.compareAndSet(false, true)) {
            return Result.failure(IllegalStateException("Reload already in progress"))
        }

        return try {
            runCatching {
                twitchConfigManager.reloadConfig()

                // Rebuild Twitch client and re-register handlers against the new instance.
                twitchClientManager.close()
                val twitchConnected = twitchClientManager.init()
                if (!twitchConnected) {
                    error("Twitch connect failed; check twitch.config.yml")
                }

                twitchEventHandler =
                    TwitchEventHandler(
                        this,
                        twitchClientManager.getTwitchClient(),
                        twitchConfigManager,
                        twitchClientManager,
                    )
                twitchEventHandler.registerEventHandlers()
            }
        } finally {
            twitchReloadInProgress.set(false)
        }
    }

    fun reloadConfigOnly(): Result<Unit> =
        runCatching {
            twitchConfigManager.reloadConfig()
        }

    fun trySendTwitchMessage(channel: String, message: String): Boolean {
        val client = runCatching { twitchClientManager.getTwitchClient() }.getOrNull() ?: return false
        return runCatching {
            client.chat.sendMessage(channel, message)
        }.isSuccess
    }

    fun resolveLinkedUserIdByUserName(userName: String): String? = npcLinkManager.resolveUserIdByUserName(userName)

    fun getNpcByUserId(userId: String): Villager? = npcLinkManager.findLoadedNpcByUserId(userId)

    fun getStoredLinkedUserName(userId: String): String? = npcLinkManager.getStoredUserName(userId)

    /**
     * Starts a simple Twitch NPC duel.
     * - Each NPC has 10 hearts (10 hits)
     * - Each successful hit removes 1 heart
     * - Random hit/miss each round
     * - Loser NPC is removed and respawned at the spawn point after 10 seconds
     */
    fun startNpcDuel(
        userAId: String,
        userAName: String,
        userBId: String,
        userBName: String,
        announce: (String) -> Unit,
    ): Result<Unit> {
        if (userAId == userBId) return Result.failure(IllegalArgumentException("Cannot duel same user"))

        val baseSpawn = pickNpcSpawnPointSpawnLocation()
            ?: return Result.failure(IllegalStateException("No NPC Spawn Point is placed"))

        // Only one duel at a time for now (keeps behavior predictable).
        twitchNpcDuelTask?.cancel()
        twitchNpcDuelTask = null

        // Spawn/teleport both NPCs near each other.
        val spawnA = baseSpawn.clone().add(-1.0, 0.0, 0.0)
        val spawnB = baseSpawn.clone().add(1.0, 0.0, 0.0)

        ensureNpcAtSpawnPoint(userAId, userAName, spawnA)
        ensureNpcAtSpawnPoint(userBId, userBName, spawnB)

        val npcA = npcLinkManager.findLoadedNpcByUserId(userAId)
        val npcB = npcLinkManager.findLoadedNpcByUserId(userBId)
        if (npcA == null || npcB == null) {
            return Result.failure(IllegalStateException("Could not load both NPCs"))
        }

        // Duel HP is tracked in-plugin (NPCs are invulnerable by design).
        var hpA = 10
        var hpB = 10
        val hitChance = 0.65
        val speed = 1.15
        val attackRange = 1.9
        val attackRangeSq = attackRange * attackRange
        val damageAmount = 2.0 // 1 heart

        fun label(name: String) = "@${name}".trim()

        announce("${label(userAName)} vs ${label(userBName)} — duel begins!")

        // Make sure they can actually take damage during the duel.
        npcA.isInvulnerable = false
        npcB.isInvulnerable = false
        npcA.health = npcA.maxHealth
        npcB.health = npcB.maxHealth

        val task = server.scheduler.runTaskTimer(
            this,
            Runnable {
                if (!npcA.isValid || !npcB.isValid) {
                    twitchNpcDuelTask?.cancel()
                    twitchNpcDuelTask = null
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
                twitchNpcDuelTask?.cancel()
                twitchNpcDuelTask = null

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

                server.scheduler.runTaskLater(
                    this,
                    Runnable {
                        val respawnLoc = pickNpcSpawnPointSpawnLocation() ?: baseSpawn
                        ensureNpcAtSpawnPoint(loserId, loserName, respawnLoc)
                        announce("${label(loserName)} respawned at the spawn point.")

                        // Best-effort: nudge winner back too so both are at spawn.
                        val winnerNpc: Villager? = npcLinkManager.findLoadedNpcByUserId(winnerId)
                        winnerNpc?.let { if (it.isValid) it.teleport(respawnLoc.clone().add(1.0, 0.0, 0.0)) }
                    },
                    20L * 10L,
                )
            },
            0L,
            20L,
        )

        twitchNpcDuelTask = task
        return Result.success(Unit)
    }

    data class TwitchStatusSnapshot(
        val connected: Boolean,
        val channelName: String?,
        val hasRequiredConfig: Boolean,
        val envPresence: Map<String, Boolean>,
    )

    fun twitchStatusSnapshot(): TwitchStatusSnapshot {
        val cfg = twitchConfigManager.getConfig()
        return TwitchStatusSnapshot(
            connected = twitchClientManager.isConnected(),
            channelName = cfg.channelName,
            hasRequiredConfig = twitchConfigManager.hasRequiredConfig(),
            envPresence = twitchConfigManager.getTwitchEnvPresence(),
        )
    }
}
