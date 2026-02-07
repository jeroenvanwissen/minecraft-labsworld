package nl.jeroenlabs.labsWorld.twitch.commands

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import nl.jeroenlabs.labsWorld.twitch.TwitchContext

class CommandInvocation(
    val context: TwitchContext,
    val event: ChannelMessageEvent,
    val commandName: String,
    val args: List<String>,
) {
    val channelName: String = event.channel.name
    val broadcasterId: String = event.channel.id
    val userId: String = event.user.id ?: event.user.name
    val userName: String = event.user.name
    val message: String = event.message

    fun reply(text: String) {
        context.twitchClient.chat.sendMessage(channelName, text)
    }

    fun replyMention(text: String) {
        reply("@${userName} $text")
    }
}
