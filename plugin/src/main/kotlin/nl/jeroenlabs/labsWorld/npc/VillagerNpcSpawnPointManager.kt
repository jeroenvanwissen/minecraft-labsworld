package nl.jeroenlabs.labsWorld.npc

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.UUID

class VillagerNpcSpawnPointManager(
    private val plugin: JavaPlugin,
) {
    private val itemKey = NamespacedKey(plugin, "npc_spawn_point_item")
    private val blockKey = NamespacedKey(plugin, "npc_spawn_point_block")

    private val storageFile = File(plugin.dataFolder, "npc-spawnpoints.yml")
    private var spawnPoints: MutableSet<SpawnPointKey> = mutableSetOf()

    data class SpawnPointKey(
        val worldId: UUID,
        val x: Int,
        val y: Int,
        val z: Int,
    ) {
        override fun toString(): String = "$worldId:$x:$y:$z"

        companion object {
            fun fromString(raw: String): SpawnPointKey? {
                val parts = raw.split(':')
                if (parts.size != 4) return null
                val worldId = runCatching { UUID.fromString(parts[0]) }.getOrNull() ?: return null
                val x = parts[1].toIntOrNull() ?: return null
                val y = parts[2].toIntOrNull() ?: return null
                val z = parts[3].toIntOrNull() ?: return null
                return SpawnPointKey(worldId, x, y, z)
            }
        }
    }

    fun init() {
        load()
    }

    fun createSpawnPointItem(amount: Int = 1): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD, amount)
        val meta = item.itemMeta
        meta.displayName(Component.text("NPC Spawn Point"))
        meta.lore(
            listOf(
                Component.text("Place this block to mark a spawn location."),
                Component.text("Break it to pick it up again."),
            ),
        )
        meta.persistentDataContainer.set(itemKey, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }

    fun isSpawnPointItem(item: ItemStack?): Boolean {
        val meta = item?.itemMeta ?: return false
        return meta.persistentDataContainer.has(itemKey, PersistentDataType.BYTE)
    }

    fun isSpawnPointBlock(block: Block): Boolean {
        val state = block.state
        if (state is TileState) {
            return state.persistentDataContainer.has(blockKey, PersistentDataType.BYTE)
        }
        // Fallback to persisted registry in case block state isn't available.
        return spawnPoints.contains(block.location.toSpawnPointKey())
    }

    fun markPlacedBlock(block: Block) {
        val state = block.state
        require(state is TileState) { "Spawn point block must be a TileState (got ${state::class.qualifiedName})" }

        state.persistentDataContainer.set(blockKey, PersistentDataType.BYTE, 1)
        state.update(true, false)

        spawnPoints.add(block.location.toSpawnPointKey())
        save()
    }

    fun unmarkBrokenBlock(block: Block) {
        spawnPoints.remove(block.location.toSpawnPointKey())
        save()
    }

    fun canUseSpawnPoints(player: Player): Boolean =
        player.hasPermission("labsworld.admin") || player.hasPermission("labsworld.npcspawnpoint")

    fun getSpawnPointLocations(): List<Location> {
        return spawnPoints.mapNotNull { key ->
            val world = Bukkit.getWorld(key.worldId) ?: return@mapNotNull null
            Location(world, key.x.toDouble(), key.y.toDouble(), key.z.toDouble())
        }
    }

    fun reconcileStoredSpawnPoints() {
        val iterator = spawnPoints.iterator()
        var changed = false
        while (iterator.hasNext()) {
            val key = iterator.next()
            val world = Bukkit.getWorld(key.worldId) ?: continue
            val block = world.getBlockAt(key.x, key.y, key.z)
            if (!isSpawnPointBlock(block)) {
                iterator.remove()
                changed = true
            }
        }
        if (changed) save()
    }

    private fun Location.toSpawnPointKey(): SpawnPointKey {
        val worldId = world?.uid ?: error("Location has no world")
        return SpawnPointKey(worldId, blockX, blockY, blockZ)
    }

    private fun load() {
        if (!storageFile.exists()) {
            spawnPoints = mutableSetOf()
            save()
            return
        }

        val cfg = YamlConfiguration.loadConfiguration(storageFile)
        val rawList = cfg.getStringList("spawn_points")
        spawnPoints = rawList.mapNotNull { SpawnPointKey.fromString(it) }.toMutableSet()
    }

    private fun save() {
        val cfg = YamlConfiguration()
        cfg.set("spawn_points", spawnPoints.map { it.toString() }.sorted())
        cfg.save(storageFile)
    }

    /**
     * Picks the first available spawn point location (sorted deterministically).
     * Reconciles stored spawn points before picking.
     * Returns a location on TOP of the marker block (offset +0.5x, +1y, +0.5z).
     */
    fun pickSpawnLocation(): Location? {
        reconcileStoredSpawnPoints()

        val points = getSpawnPointLocations()
        val chosen = points
            .sortedWith(
                compareBy<Location>({ it.world?.uid?.toString() ?: "" }, { it.blockX }, { it.blockY }, { it.blockZ }),
            )
            .firstOrNull() ?: return null

        // Spawn on top of the marker block.
        return chosen.clone().add(0.5, 1.0, 0.5)
    }
}
