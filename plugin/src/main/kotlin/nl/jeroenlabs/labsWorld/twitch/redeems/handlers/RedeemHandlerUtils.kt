package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation
import org.bukkit.plugin.java.JavaPlugin

fun pluginAsLabsWorld(plugin: JavaPlugin): LabsWorld? = plugin as? LabsWorld

fun renderTemplate(template: String, invocation: RedeemInvocation): String =
    template
        .replace("{user}", invocation.userName)
        .replace("{userId}", invocation.userId)
        .replace("{reward}", invocation.rewardTitle)
        .replace("{rewardId}", invocation.rewardId)
        .replace("{input}", invocation.userInput ?: "")


