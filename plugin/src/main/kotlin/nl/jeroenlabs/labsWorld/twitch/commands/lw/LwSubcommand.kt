package nl.jeroenlabs.labsWorld.twitch.commands.lw

import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.TwitchContext
import nl.jeroenlabs.labsWorld.twitch.commands.CommandInvocation
import nl.jeroenlabs.labsWorld.twitch.commands.Permission

interface LwSubcommand {
    val name: String
    val aliases: Set<String> get() = emptySet()
    val permission: Permission get() = Permission.EVERYONE
    val runOnMainThread: Boolean get() = true
    fun handle(ctx: TwitchContext, inv: CommandInvocation)
}

// Shared helpers for subcommands
fun TwitchContext.labsWorld(): LabsWorld = plugin

fun parseDuration(arg: String?, default: Int = 30): Int =
    (arg?.toIntOrNull() ?: default).coerceIn(1, 300)

fun parseDamage(arg: String?, default: Double = 1.0): Double =
    (arg?.toDoubleOrNull() ?: default).coerceIn(0.5, 10.0)

fun sanitizeTwitchName(raw: String): String {
    val noAt = raw.trim().removePrefix("@")
    return noAt.takeWhile { it.isLetterOrDigit() || it == '_' }
}
