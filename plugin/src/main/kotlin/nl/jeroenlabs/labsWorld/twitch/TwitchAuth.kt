package nl.jeroenlabs.labsWorld.twitch

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import nl.jeroenlabs.labsWorld.twitch.commands.Permission

/**
 * Single source of truth for Twitch permission and authorization checking.
 */
object TwitchAuth {
    /**
     * Reads a single IRC tag value from a Twitch4J chat event.
     */
    fun getTagValue(event: ChannelMessageEvent, key: String): String? =
        runCatching { event.messageEvent.getTagValue(key).orElse(null) }.getOrNull()

    fun isBroadcaster(event: ChannelMessageEvent): Boolean =
        runCatching { event.user.id == event.channel.id }.getOrDefault(false)

    fun isModerator(event: ChannelMessageEvent): Boolean =
        getTagValue(event, "mod") == "1" ||
            (getTagValue(event, "badges")?.contains("moderator/") == true)

    fun isBroadcasterOrModerator(event: ChannelMessageEvent): Boolean =
        isBroadcaster(event) || isModerator(event)

    /**
     * Checks whether the user behind [event] satisfies the [required] permission level.
     */
    fun isAuthorized(required: Permission, event: ChannelMessageEvent): Boolean {
        if (required == Permission.EVERYONE) return true

        if (isBroadcaster(event)) return true

        val badges = getTagValue(event, "badges")
        val isMod = getTagValue(event, "mod") == "1" || (badges?.contains("moderator/") == true)
        val isVip = getTagValue(event, "vip") == "1" || (badges?.contains("vip/") == true)
        val isSubscriber = getTagValue(event, "subscriber") == "1" || (badges?.contains("subscriber/") == true)

        return when (required) {
            Permission.BROADCASTER -> false
            Permission.MODERATOR -> isMod
            Permission.VIP -> isMod || isVip
            Permission.SUBSCRIBER -> isMod || isVip || isSubscriber
            Permission.EVERYONE -> true
        }
    }
}
