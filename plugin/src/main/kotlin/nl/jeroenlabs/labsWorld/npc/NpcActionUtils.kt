package nl.jeroenlabs.labsWorld.npc

import nl.jeroenlabs.labsWorld.LabsWorld
import org.bukkit.entity.Player
import kotlin.random.Random

internal fun pickTargetPlayer(plugin: LabsWorld, preferred: String?, allowRandom: Boolean): Player? {
    val online = plugin.server.onlinePlayers.toList()
    if (!preferred.isNullOrBlank()) {
        return plugin.server.getPlayerExact(preferred)
    }
    if (online.isEmpty()) return null
    if (online.size == 1) return online.first()
    if (!allowRandom) return null
    return online[Random.nextInt(online.size)]
}

internal fun clampDurationSeconds(value: Int?, defaultSeconds: Int = 30): Int =
    (value ?: defaultSeconds).coerceIn(1, 300)

internal fun clampDamageHearts(value: Double?, defaultHearts: Double = 1.0): Double =
    (value ?: defaultHearts).coerceIn(0.5, 10.0)

internal fun parseDurationSeconds(arg: String?, defaultSeconds: Int = 30): Int =
    clampDurationSeconds(arg?.toIntOrNull(), defaultSeconds)

internal fun parseDamageHearts(arg: String?, defaultHearts: Double = 1.0): Double =
    clampDamageHearts(arg?.toDoubleOrNull(), defaultHearts)
