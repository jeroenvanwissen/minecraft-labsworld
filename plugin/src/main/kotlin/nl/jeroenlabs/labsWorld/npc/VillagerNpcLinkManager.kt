package nl.jeroenlabs.labsWorld.npc

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Entity
import org.bukkit.entity.Villager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.UUID

class VillagerNpcLinkManager(
    private val plugin: JavaPlugin,
    private val npcManager: VillagerNpcManager,
) {
    private val storageFile = File(plugin.dataFolder, "twitch-npcs.yml")

    fun init() {
        if (!storageFile.exists()) {
            save(YamlConfiguration())
        }
    }

    data class EnsureResult(
        val spawned: Boolean,
        val npcUuid: UUID,
        val npcName: String?,
    )

    /**
     * Ensures the given linked user has exactly one NPC, and that it's at the given location.
     * If an NPC already exists for that user, it will be teleported to the spawn location.
     */
    fun ensureNpcAt(
        userId: String,
        userName: String,
        spawnLocation: Location,
        profession: Villager.Profession? = null,
    ): EnsureResult {
        val cfg = load()

        // First: try to find an already-loaded NPC tagged for this user (even if file is stale).
        findLoadedNpcByUserId(userId)?.let { existing ->
            applyNpcRuntimeSettings(existing, profession)
            existing.teleport(spawnLocation)
            persist(cfg, userId, userName, existing.uniqueId, spawnLocation)
            save(cfg)
            return EnsureResult(spawned = false, npcUuid = existing.uniqueId, npcName = existing.customName)
        }

        val path = "users.$userId"
        val uuidStr = cfg.getString("$path.npc_uuid")
        val existingUuid = uuidStr?.let { runCatching { UUID.fromString(it) }.getOrNull() }

        if (existingUuid != null) {
            val entity = plugin.server.getEntity(existingUuid)
            val villager = entity as? Villager
            if (villager != null && isLinkedToUser(villager, userId)) {
                applyNpcRuntimeSettings(villager, profession)
                villager.teleport(spawnLocation)
                persist(cfg, userId, userName, villager.uniqueId, spawnLocation)
                save(cfg)
                return EnsureResult(spawned = false, npcUuid = villager.uniqueId, npcName = villager.customName)
            }

            // If it's not loaded, try to load the chunk where we last stored it.
            val storedLoc = readStoredLocation(cfg, userId)
            if (storedLoc != null) {
                val found = tryLoadAndFindNpcByUuid(existingUuid, storedLoc)
                if (found is Villager && isLinkedToUser(found, userId)) {
                    applyNpcRuntimeSettings(found, profession)
                    found.teleport(spawnLocation)
                    persist(cfg, userId, userName, found.uniqueId, spawnLocation)
                    save(cfg)
                    return EnsureResult(spawned = false, npcUuid = found.uniqueId, npcName = found.customName)
                }
            }

            // Stale mapping: clear it and continue to spawn a new NPC.
            cfg.set(path, null)
            save(cfg)
        }

        val npc = npcManager.createLinkedNpc(spawnLocation, userId, userName, profession = Villager.Profession.NONE)
        persist(cfg, userId, userName, npc.uniqueId, spawnLocation)
        save(cfg)
        return EnsureResult(spawned = true, npcUuid = npc.uniqueId, npcName = npc.customName)
    }

    private fun applyNpcRuntimeSettings(villager: Villager, profession: Villager.Profession? = null) {
        // Ensure NPCs can wander even if they were created when AI was disabled.
        villager.setAI(true)

        if (profession != null) {
            villager.profession = profession
        }

        // Keep prior behavior stable/persistent.
        villager.removeWhenFarAway = false
    }

    private fun isLinkedToUser(villager: Villager, userId: String): Boolean {
        return VillagerNpcKeys.isLinkedToUser(villager, userId, plugin)
    }

    fun findLoadedNpcByUserId(userId: String): Villager? {
        return plugin.server.worlds
            .asSequence()
            .flatMap { it.livingEntities.asSequence() }
            .filterIsInstance<Villager>()
            .firstOrNull { isLinkedToUser(it, userId) }
    }

    fun getStoredUserName(userId: String): String? {
        val cfg = load()
        val base = resolveUserBasePath(cfg, userId) ?: "users.$userId"
        return cfg.getString("$base.user_name")
    }

    /**
     * Resolves a linked userId by stored user_name in twitch-npcs.yml.
     * This only works after that user has spawned/linked their NPC at least once.
     */
    fun resolveUserIdByUserName(userName: String): String? {
        val cfg = load()
        val normalized = userName.trim()

        val usersSection = cfg.getConfigurationSection("users")
        if (usersSection != null) {
            for (id in usersSection.getKeys(false)) {
                val stored = cfg.getString("users.$id.user_name") ?: continue
                if (stored.equals(normalized, ignoreCase = true)) {
                    return id
                }
            }
            return null
        }

        for (id in cfg.getKeys(false)) {
            if (id == "users") continue
            val stored = cfg.getString("$id.user_name") ?: continue
            if (stored.equals(normalized, ignoreCase = true)) {
                return id
            }
        }

        return null
    }

    private fun readStoredLocation(cfg: YamlConfiguration, userId: String): Location? {
        val path = resolveUserBasePath(cfg, userId) ?: "users.$userId"
        val worldIdStr = cfg.getString("$path.world_id") ?: return null
        val worldId = runCatching { UUID.fromString(worldIdStr) }.getOrNull() ?: return null
        val world = Bukkit.getWorld(worldId) ?: return null

        val x = cfg.getDouble("$path.x")
        val y = cfg.getDouble("$path.y")
        val z = cfg.getDouble("$path.z")

        return Location(world, x, y, z)
    }

    private fun resolveUserBasePath(cfg: YamlConfiguration, userId: String): String? {
        val usersSection = cfg.getConfigurationSection("users")
        if (usersSection != null) {
            return if (usersSection.contains(userId)) "users.$userId" else null
        }
        return if (cfg.contains(userId)) userId else null
    }

    private fun tryLoadAndFindNpcByUuid(uuid: UUID, approxLocation: Location): Entity? {
        val world = approxLocation.world ?: return null
        val chunk = world.getChunkAt(approxLocation)
        if (!chunk.isLoaded) {
            chunk.load(true)
        }
        return chunk.entities.firstOrNull { it.uniqueId == uuid }
    }

    private fun persist(
        cfg: YamlConfiguration,
        userId: String,
        userName: String,
        npcUuid: UUID,
        location: Location,
    ) {
        val worldId = location.world?.uid ?: return
        val path = "users.$userId"
        cfg.set("$path.user_name", userName)
        cfg.set("$path.npc_uuid", npcUuid.toString())
        cfg.set("$path.world_id", worldId.toString())
        cfg.set("$path.x", location.x)
        cfg.set("$path.y", location.y)
        cfg.set("$path.z", location.z)
        cfg.set("$path.updated_at", System.currentTimeMillis())
    }

    private fun load(): YamlConfiguration {
        if (!storageFile.exists()) {
            return YamlConfiguration()
        }
        return YamlConfiguration.loadConfiguration(storageFile)
    }

    private fun save(cfg: YamlConfiguration) {
        cfg.save(storageFile)
    }
}
