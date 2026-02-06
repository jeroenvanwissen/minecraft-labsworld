package nl.jeroenlabs.labsWorld

import nl.jeroenlabs.labsWorld.npc.VillagerNpcAggroService
import nl.jeroenlabs.labsWorld.npc.VillagerNpcDuelService
import nl.jeroenlabs.labsWorld.npc.VillagerNpcLinkManager
import nl.jeroenlabs.labsWorld.npc.VillagerNpcManager
import nl.jeroenlabs.labsWorld.npc.VillagerNpcSpawnPointListener
import nl.jeroenlabs.labsWorld.npc.VillagerNpcSpawnPointManager
import nl.jeroenlabs.labsWorld.twitch.TwitchClientManager
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import nl.jeroenlabs.labsWorld.twitch.TwitchEventHandler
import nl.jeroenlabs.labsWorld.commands.LabsWorldPaperCommand
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.atomic.AtomicBoolean

class LabsWorld : JavaPlugin() {
    private lateinit var twitchConfigManager: TwitchConfigManager
    private lateinit var twitchClientManager: TwitchClientManager
    private lateinit var twitchEventHandler: TwitchEventHandler
    private lateinit var npcManager: VillagerNpcManager
    private lateinit var npcSpawnPointManager: VillagerNpcSpawnPointManager
    private lateinit var npcLinkManager: VillagerNpcLinkManager
    private lateinit var npcAggroService: VillagerNpcAggroService
    private lateinit var npcDuelService: VillagerNpcDuelService

    private val twitchReloadInProgress = AtomicBoolean(false)

    override fun onEnable() {
        dataFolder.mkdirs()
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

        npcManager = VillagerNpcManager(this)

        npcLinkManager = VillagerNpcLinkManager(this, npcManager)
        npcLinkManager.init()

        npcAggroService = VillagerNpcAggroService(this, npcLinkManager)

        npcSpawnPointManager = VillagerNpcSpawnPointManager(this)
        npcSpawnPointManager.init()
        server.pluginManager.registerEvents(VillagerNpcSpawnPointListener(npcSpawnPointManager), this)
        server.scheduler.runTask(this, Runnable { npcSpawnPointManager.reconcileStoredSpawnPoints() })

        npcDuelService = VillagerNpcDuelService(this, npcLinkManager, npcSpawnPointManager)

        registerCommand("labsworld", LabsWorldPaperCommand(this))
    }

    fun createNpcSpawnPointItem(amount: Int = 1) = npcSpawnPointManager.createSpawnPointItem(amount)

    fun npcSpawnPointCount(): Int = npcSpawnPointManager.getSpawnPointLocations().size

    fun pickNpcSpawnPointSpawnLocation(): Location? = npcSpawnPointManager.pickSpawnLocation()

    fun ensureNpcAtSpawnPoint(
        userId: String,
        userName: String,
        spawnLocation: Location,
    ): Result<String> = npcLinkManager.ensureNpcAtWithChunkLoad(userId, userName, spawnLocation)

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
    ): Result<Unit> = npcDuelService.startDuel(userAId, userAName, userBId, userBName, announce)

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
