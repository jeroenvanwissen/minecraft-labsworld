package nl.jeroenlabs.labsWorld.twitch.actions

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import nl.jeroenlabs.labsWorld.util.anyToBool
import nl.jeroenlabs.labsWorld.util.anyToInt
import nl.jeroenlabs.labsWorld.util.anyToString
import nl.jeroenlabs.labsWorld.util.PlayerUtils
import kotlin.random.Random

/**
 * Shared utility functions used by action handlers.
 * Extracted from ActionExecutor to allow reuse across individual handler classes.
 */
object ActionUtils {

    /**
     * Resolve the target player for an action.
     * Checks params["target_player"], falls back to the invoking Twitch user name,
     * and finally picks a random online player if allowed.
     */
    fun resolveTargetPlayer(invocation: ActionInvocation, params: Map<String, Any?>): Player? {
        val targetRaw = anyToString(params["target_player"])
        val allowRandom = anyToBool(params["allow_random"], true) ?: true

        val targetName = when (targetRaw?.lowercase()) {
            null, "" -> null
            "redeemer", "invoker", "self", "{user}" -> invocation.userName
            else -> targetRaw
        }

        if (!targetName.isNullOrBlank()) {
            return Bukkit.getPlayerExact(targetName)
        }

        // Try matching the Twitch user name to an online player.
        Bukkit.getPlayerExact(invocation.userName)?.let { return it }

        return PlayerUtils.pickTargetPlayer(Bukkit.getServer(), preferred = null, allowRandom = allowRandom)
    }

    /** Generate a random XZ offset within the given radius. */
    fun randomOffset(radius: Double): Vector =
        if (radius <= 0.0) Vector(0.0, 0.0, 0.0) else {
            val dx = (Random.nextDouble() * 2.0 - 1.0) * radius
            val dz = (Random.nextDouble() * 2.0 - 1.0) * radius
            Vector(dx, 0.0, dz)
        }

    /** Parse a list of dye-color names into Bukkit [Color] instances. */
    fun parseColors(raw: List<String>): List<Color> =
        raw.mapNotNull { name ->
            runCatching { DyeColor.valueOf(name.trim().uppercase()) }.getOrNull()?.color
        }

    /** Map a shape name string to a [FireworkEffect.Type]. */
    fun parseFireworkType(shape: String): FireworkEffect.Type =
        when (shape) {
            "ball" -> FireworkEffect.Type.BALL
            "ball_large", "large_ball", "large" -> FireworkEffect.Type.BALL_LARGE
            "star" -> FireworkEffect.Type.STAR
            "burst" -> FireworkEffect.Type.BURST
            "creeper" -> FireworkEffect.Type.CREEPER
            else -> FireworkEffect.Type.BALL
        }

    /** Parse a mob name string into an [EntityType], or null if unrecognised. */
    fun parseEntityType(name: String): EntityType? {
        EntityType.fromName(name)?.let { return it }
        return runCatching { EntityType.valueOf(name.trim().uppercase()) }.getOrNull()
    }

    /** Parse a raw list of item maps into [ItemStack] instances. */
    fun parseItemStacks(raw: Any?): List<ItemStack> {
        if (raw !is List<*>) return emptyList()
        return raw.mapNotNull { entry ->
            val map = entry as? Map<*, *> ?: return@mapNotNull null
            val typeName = anyToString(map["type"]) ?: return@mapNotNull null
            val material = Material.matchMaterial(typeName) ?: return@mapNotNull null
            val amount = anyToInt(map["amount"], 1).coerceAtLeast(1)
            ItemStack(material, amount)
        }
    }

    /** Pick the first available world (overworld). */
    fun pickDefaultWorld(worlds: List<World>): World? = worlds.firstOrNull()
}
