package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

fun pluginAsLabsWorld(plugin: JavaPlugin): LabsWorld? = plugin as? LabsWorld

fun renderTemplate(template: String, invocation: RedeemInvocation): String =
    template
        .replace("{user}", invocation.userName)
        .replace("{userId}", invocation.userId)
        .replace("{reward}", invocation.rewardTitle)
        .replace("{rewardId}", invocation.rewardId)
        .replace("{input}", invocation.userInput ?: "")

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
