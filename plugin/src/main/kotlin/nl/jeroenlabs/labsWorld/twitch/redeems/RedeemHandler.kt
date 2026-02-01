package nl.jeroenlabs.labsWorld.twitch.redeems

import com.github.twitch4j.TwitchClient
import nl.jeroenlabs.labsWorld.twitch.TwitchConfigManager
import org.bukkit.plugin.java.JavaPlugin

interface RedeemHandler {
    /** Unique, config-facing handler key, e.g. "npc.spawn". */
    val key: String

    /** Whether Bukkit work should run on the main thread. */
    val runOnMainThread: Boolean get() = true

    fun handle(context: RedeemHandlerContext, invocation: RedeemInvocation, params: Map<String, Any?>)
}

/** Context passed to redeem handlers with access to plugin and Twitch client. */
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
