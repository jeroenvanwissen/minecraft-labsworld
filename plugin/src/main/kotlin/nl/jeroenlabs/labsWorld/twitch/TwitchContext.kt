package nl.jeroenlabs.labsWorld.twitch

import com.github.twitch4j.TwitchClient
import nl.jeroenlabs.labsWorld.LabsWorld

/**
 * Shared context passed to commands, redeem handlers, and action executors.
 * Replaces the former CommandContext, RedeemHandlerContext, and ActionContext.
 */
data class TwitchContext(
    val plugin: LabsWorld,
    val twitchClient: TwitchClient,
    val twitchConfigManager: TwitchConfigManager,
)

/** Send a chat message to the configured channel. */
fun TwitchContext.say(message: String) {
    val channel = twitchConfigManager.getConfig().channelName ?: return
    runCatching { twitchClient.chat.sendMessage(channel, message) }
}
