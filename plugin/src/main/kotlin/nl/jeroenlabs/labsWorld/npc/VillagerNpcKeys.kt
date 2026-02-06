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

    /** Key for the owner UUID of a custom NPC (value: UUID string). */
    fun owner(plugin: Plugin): NamespacedKey = NamespacedKey(plugin, "npc_owner")

    /** Key for the linked Twitch user ID (value: user ID string). */
    fun twitchUserId(plugin: Plugin): NamespacedKey = NamespacedKey(plugin, "npc_twitch_user_id")

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
}
