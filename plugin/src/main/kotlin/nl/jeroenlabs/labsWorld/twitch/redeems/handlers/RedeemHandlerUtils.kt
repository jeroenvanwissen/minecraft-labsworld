package nl.jeroenlabs.labsWorld.twitch.redeems.handlers

import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemInvocation

fun renderTemplate(template: String, invocation: RedeemInvocation): String =
    template
        .replace("{user}", invocation.userName)
        .replace("{userId}", invocation.userId)
        .replace("{reward}", invocation.rewardTitle)
        .replace("{rewardId}", invocation.rewardId)
        .replace("{input}", invocation.userInput ?: "")


