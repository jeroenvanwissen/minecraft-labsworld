package nl.jeroenlabs.labsWorld.twitch

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import nl.jeroenlabs.labsWorld.twitch.commands.Permission

/**
 * Single source of truth for Twitch permission and authorization checking.
 */
object TwitchAuth {
    /**
     * Extracts IRC tags from a Twitch4J chat event via the direct API.
     */
    fun getIrcTags(event: ChannelMessageEvent): Map<String, String> {
        val tags = runCatching { event.messageEvent.tags }.getOrNull() ?: return emptyMap()
        return tags.mapValues { (_, v) -> v ?: "" }
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
