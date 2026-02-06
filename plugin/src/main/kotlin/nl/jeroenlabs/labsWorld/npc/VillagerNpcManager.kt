package nl.jeroenlabs.labsWorld.npc

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
class VillagerNpcManager(
    private val plugin: JavaPlugin,
) {
    /**
     * Creates an NPC linked to a Twitch user.
     */
    fun createLinkedNpc(
        location: Location,
        userId: String,
        userName: String,
        profession: Villager.Profession? = null,
    ): Villager {
        val npc = spawnBaseVillagerNpc(location, userName, profession)
        npc.persistentDataContainer.set(VillagerNpcKeys.twitchUserId(plugin), PersistentDataType.STRING, userId)
        return npc
    }

    /**
     * Spawns a base Villager NPC with common configuration.
     * All NPCs are marked as custom, have AI enabled but are invulnerable and silent.
     */
    private fun spawnBaseVillagerNpc(
        location: Location,
        name: String?,
        profession: Villager.Profession?,
    ): Villager {
        val npc = location.world.spawnEntity(location, EntityType.VILLAGER) as Villager

        // Mark as custom NPC
        npc.persistentDataContainer.set(VillagerNpcKeys.customNpcTag(plugin), PersistentDataType.BYTE, 1)

        // Set display name if provided
        if (!name.isNullOrEmpty()) {
            val uniqueName = generateUniqueName(name)
            npc.customName = uniqueName
            npc.isCustomNameVisible = true
        }

        // Set profession if provided
        if (profession != null) {
            npc.profession = profession
        }

        // Configure NPC behavior: AI enabled, but invulnerable and silent
        npc.setAI(true)
        npc.isInvulnerable = true
        npc.isSilent = true
        npc.removeWhenFarAway = false

        return npc
    }

    private fun generateUniqueName(baseName: String): String {
        val existingNames = mutableSetOf<String>()

        plugin.server.worlds.forEach { world ->
            world.livingEntities.filterIsInstance<Villager>().forEach { villager ->
                villager.customName()?.let { component ->
                    val name = PlainTextComponentSerializer.plainText().serialize(component)
                    existingNames.add(name)
                }
            }
        }

        if (!existingNames.contains(baseName)) {
            return baseName
        }

        var counter = 1
        var uniqueName: String
        do {
            uniqueName = "$baseName #$counter"
            counter++
        } while (existingNames.contains(uniqueName))

        return uniqueName
    }

    fun isCustomNpc(entity: Villager): Boolean = VillagerNpcKeys.isCustomNpc(entity, plugin)

    fun getAllCustomNpcs(): List<Villager> =
        plugin.server.worlds.flatMap { world ->
            world.livingEntities.filterIsInstance<Villager>().filter { isCustomNpc(it) }
        }
}
