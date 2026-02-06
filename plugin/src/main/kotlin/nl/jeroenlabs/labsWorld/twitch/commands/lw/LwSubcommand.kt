package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.commands.CommandContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation
import org.bukkit.entity.Player
import kotlin.random.Random

interface LwSubcommand {
    val name: String
    val aliases: Set<String> get() = emptySet()
    fun handle(ctx: CommandContext, inv: CommandInvocation)
}

// Shared helpers for subcommands
fun CommandContext.labsWorld(): LabsWorld? = plugin as? LabsWorld

fun LabsWorld.pickTargetPlayer(preferred: String?, allowRandom: Boolean): Player? {
    val online = server.onlinePlayers.toList()
    if (!preferred.isNullOrBlank()) {
        val exact = server.getPlayerExact(preferred)
        if (exact != null) return exact
        if (!allowRandom) return null
    }
    if (online.isEmpty()) return null
    if (online.size == 1) return online.first()
    if (!allowRandom) return null
    return online[Random.nextInt(online.size)]
}

fun parseDuration(arg: String?, default: Int = 30): Int =
    (arg?.toIntOrNull() ?: default).coerceIn(1, 300)

fun parseDamage(arg: String?, default: Double = 1.0): Double =
    (arg?.toDoubleOrNull() ?: default).coerceIn(0.5, 10.0)

fun sanitizeTwitchName(raw: String): String {
    val noAt = raw.trim().removePrefix("@")
    return noAt.takeWhile { it.isLetterOrDigit() || it == '_' }
}
