package nl.jeroenlabs.labsWorld.twitch

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.eventsub.events.ChannelPointsCustomRewardRedemptionEvent
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes
import nl.jeroenlabs.labsWorld.LabsWorld
import nl.jeroenlabs.labsWorld.twitch.commands.CommandDispatcher
import nl.jeroenlabs.labsWorld.twitch.commands.LwCommand
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemDispatcher
import nl.jeroenlabs.labsWorld.twitch.redeems.handlers.RedeemHandlers
import org.bukkit.plugin.java.JavaPlugin

class TwitchEventHandler(
    private val plugin: JavaPlugin,
    private val twitchClient: TwitchClient,
    private val twitchConfigManager: TwitchConfigManager,
    private val twitchClientManager: TwitchClientManager? = null,
) {
    private val context = TwitchContext(
        plugin = plugin as LabsWorld,
        twitchClient = twitchClient,
        twitchConfigManager = twitchConfigManager,
    )

    private val commandDispatcher = CommandDispatcher(context).also {
        it.register(LwCommand(context))
    }

    private val redeemDispatcher = RedeemDispatcher(context).also {
        RedeemHandlers.all.forEach { handler -> it.register(handler) }
    }

    fun registerEventHandlers() {
        val channelName = twitchConfigManager.getConfig().channelName ?: return

        // Register Channel Message Event
        twitchClient.eventManager.onEvent(
            ChannelMessageEvent::class.java,
            this::handleChannelMessageEvent,
        )

        // Register Channel Point Custom Reward Redemption Event
        twitchClient.eventSocket.register(
            SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD.prepareSubscription(
                { builder -> builder.broadcasterUserId(twitchClientManager?.getChannelId()).build() },
                null
            )
        )

        twitchClient.eventManager.onEvent(
            ChannelPointsCustomRewardRedemptionEvent::class.java,
            this::handleChannelPointsCustomRewardsRedemptionEvent,
        )

        plugin.logger.info("Registered Twitch event handlers")
    }

    private fun handleChannelMessageEvent(event: ChannelMessageEvent) {
        plugin.logger.info("Handling chat message from ${event.user.name}: ${event.message}")
        commandDispatcher.handle(event)
    }

    private fun handleChannelPointsCustomRewardsRedemptionEvent(event: ChannelPointsCustomRewardRedemptionEvent) {
        plugin.logger.info("Handling redeem event for reward=${event.reward.title}")
        redeemDispatcher.handle(event)
    }
}
