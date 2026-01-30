package nl.jeroenlabs.labsWorld.twitch.redeems

import com.github.twitch4j.TwitchClient
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import org.bukkit.plugin.java.JavaPlugin

class RedeemHandlerContext(
    val plugin: JavaPlugin,
    val twitchClient: TwitchClient,
    val twitchConfigManager: TwitchConfigManager,
) {
    fun say(message: String) {
        val channel = twitchConfigManager.getConfig().channelName ?: return
        runCatching { twitchClient.chat.sendMessage(channel, message) }
    }
}
