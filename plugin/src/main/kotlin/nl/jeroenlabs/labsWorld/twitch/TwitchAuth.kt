package nl.jeroenlabs.labsWorld.twitch

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import nl.jeroenlabs.labsWorld.twitch.commands.Permission

/**
 * Single source of truth for Twitch permission and authorization checking.
 */
object TwitchAuth {
    /**
     * Best-effort extraction of IRC tags from Twitch4J chat events.
     * Uses reflection to stay resilient across Twitch4J versions.
     */
    fun getIrcTags(event: Any): Map<String, String> {
        val tags =
            runCatching {
                val method = event.javaClass.methods.firstOrNull { it.name == "getTags" && it.parameterCount == 0 }
                    ?: return@runCatching null

                @Suppress("UNCHECKED_CAST")
                method.invoke(event) as? Map<*, *>
            }.getOrNull() ?: return emptyMap()

        return tags.entries
            .mapNotNull { (k, v) -> (k as? String)?.let { key -> key to (v?.toString() ?: "") } }
            .toMap()
    }

    fun isBroadcaster(event: ChannelMessageEvent): Boolean =
        runCatching { event.user.id == event.channel.id }.getOrDefault(false)

    fun isModerator(event: ChannelMessageEvent): Boolean {
        val tags = getIrcTags(event)
        return tags["mod"] == "1" || (tags["badges"]?.contains("moderator/") == true)
    }

    fun isBroadcasterOrModerator(event: ChannelMessageEvent): Boolean =
        isBroadcaster(event) || isModerator(event)

    /**
     * Checks whether the user behind [event] satisfies the [required] permission level.
     */
    fun isAuthorized(required: Permission, event: ChannelMessageEvent): Boolean {
        if (required == Permission.EVERYONE) return true

        if (isBroadcaster(event)) return true

        val tags = getIrcTags(event)
        val isMod = tags["mod"] == "1" || (tags["badges"]?.contains("moderator/") == true)
        val isVip = tags["vip"] == "1" || (tags["badges"]?.contains("vip/") == true)
        val isSubscriber = tags["subscriber"] == "1" || (tags["badges"]?.contains("subscriber/") == true)

        return when (required) {
            Permission.BROADCASTER -> false
            Permission.MODERATOR -> isMod
            Permission.VIP -> isMod || isVip
            Permission.SUBSCRIBER -> isMod || isVip || isSubscriber
            Permission.EVERYONE -> true
        }
    }
}
