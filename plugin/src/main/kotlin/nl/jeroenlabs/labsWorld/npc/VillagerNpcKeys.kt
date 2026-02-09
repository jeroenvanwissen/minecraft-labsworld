package nl.jeroenlabs.labsWorld.npc

import org.bukkit.NamespacedKey
import org.bukkit.entity.Villager
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

/**
 * Centralised NamespacedKey definitions for VillagerNpc persistence.
 * All NPC-related keys should be defined here to avoid duplication.
 */
object VillagerNpcKeys {
    /** Key to mark an entity as a custom NPC (value: 1 byte). */
    fun customNpcTag(plugin: Plugin): NamespacedKey = NamespacedKey(plugin, "custom_npc")

    /** Key for the linked Twitch user ID (value: user ID string). */
    fun twitchUserId(plugin: Plugin): NamespacedKey = NamespacedKey(plugin, "npc_twitch_user_id")

    /** Key for the NPC's accumulated XP (value: integer). */
    fun npcXp(plugin: Plugin): NamespacedKey = NamespacedKey(plugin, "npc_xp")

    /** Key for the NPC's current level (value: integer). */
    fun npcLevel(plugin: Plugin): NamespacedKey = NamespacedKey(plugin, "npc_level")

    // ─────────────────────────────────────────────────────────────────────────────
    // Helper functions for common PDC lookups
    // ─────────────────────────────────────────────────────────────────────────────

    /** Returns true if this villager is a custom NPC (has the custom_npc tag). */
    fun isCustomNpc(villager: Villager, plugin: Plugin): Boolean {
        return villager.persistentDataContainer.has(customNpcTag(plugin), PersistentDataType.BYTE)
    }

    /** Returns true if this villager is linked to a Twitch user. */
    fun isLinkedVillagerNpc(villager: Villager, plugin: Plugin): Boolean {
        return villager.persistentDataContainer.has(twitchUserId(plugin), PersistentDataType.STRING)
    }

    /** Returns the linked Twitch user ID, or null if not linked. */
    fun getLinkedUserId(villager: Villager, plugin: Plugin): String? {
        return villager.persistentDataContainer.get(twitchUserId(plugin), PersistentDataType.STRING)
    }

    /** Returns true if this villager is linked to the given Twitch user ID. */
    fun isLinkedToUser(villager: Villager, userId: String, plugin: Plugin): Boolean {
        return getLinkedUserId(villager, plugin) == userId
    }

    /** Returns the NPC's current XP, or 0 if not set. */
    fun getXp(villager: Villager, plugin: Plugin): Int {
        return villager.persistentDataContainer.get(npcXp(plugin), PersistentDataType.INTEGER) ?: 0
    }

    /** Sets the NPC's XP value. */
    fun setXp(villager: Villager, xp: Int, plugin: Plugin) {
        villager.persistentDataContainer.set(npcXp(plugin), PersistentDataType.INTEGER, xp)
    }

    /** Returns the NPC's current level, or 1 if not set. */
    fun getLevel(villager: Villager, plugin: Plugin): Int {
        return villager.persistentDataContainer.get(npcLevel(plugin), PersistentDataType.INTEGER) ?: 1
    }

    /** Sets the NPC's level value. */
    fun setLevel(villager: Villager, level: Int, plugin: Plugin) {
        villager.persistentDataContainer.set(npcLevel(plugin), PersistentDataType.INTEGER, level)
    }
}
