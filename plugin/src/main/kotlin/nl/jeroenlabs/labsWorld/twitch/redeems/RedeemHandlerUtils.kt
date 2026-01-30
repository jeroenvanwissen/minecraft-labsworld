package nl.jeroenlabs.labsWorld.twitch.redeems

import nl.jeroenlabs.labsWorld.LabsWorld
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

internal fun pluginAsLabsWorld(plugin: JavaPlugin): LabsWorld? = plugin as? LabsWorld

internal fun anyToInt(value: Any?, default: Int): Int =
    when (value) {
        is Number -> value.toInt()
        is String -> value.trim().toIntOrNull() ?: default
        else -> default
    }

internal fun anyToDouble(value: Any?, default: Double): Double =
    when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull() ?: default
        else -> default
    }

internal fun anyToString(value: Any?): String? =
    when (value) {
        null -> null
        is String -> value.trim().takeIf { it.isNotEmpty() }
        else -> value.toString().trim().takeIf { it.isNotEmpty() }
    }

internal fun renderTemplate(template: String, invocation: RedeemInvocation): String =
    template
        .replace("{user}", invocation.userName)
        .replace("{userId}", invocation.userId)
        .replace("{reward}", invocation.rewardTitle)
        .replace("{rewardId}", invocation.rewardId)
        .replace("{input}", invocation.userInput ?: "")
