package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

fun pluginAsLabsWorld(plugin: JavaPlugin): LabsWorld? = plugin as? LabsWorld

fun anyToInt(value: Any?, default: Int): Int = when (value) {
    is Number -> value.toInt()
    is String -> value.trim().toIntOrNull() ?: default
    else -> default
}

fun anyToDouble(value: Any?, default: Double): Double = when (value) {
    is Number -> value.toDouble()
    is String -> value.trim().toDoubleOrNull() ?: default
    else -> default
}

fun anyToString(value: Any?): String? = when (value) {
    null -> null
    is String -> value.trim().takeIf { it.isNotEmpty() }
    else -> value.toString().trim().takeIf { it.isNotEmpty() }
}

fun renderTemplate(template: String, invocation: RedeemInvocation): String =
    template
        .replace("{user}", invocation.userName)
        .replace("{userId}", invocation.userId)
        .replace("{reward}", invocation.rewardTitle)
        .replace("{rewardId}", invocation.rewardId)
        .replace("{input}", invocation.userInput ?: "")

fun LabsWorld.pickTargetPlayer(preferred: String?, allowRandom: Boolean): Player? {
    val online = server.onlinePlayers.toList()
    if (!preferred.isNullOrBlank()) return server.getPlayerExact(preferred)
    if (online.isEmpty()) return null
    if (online.size == 1) return online.first()
    if (!allowRandom) return null
    return online[Random.nextInt(online.size)]
}
