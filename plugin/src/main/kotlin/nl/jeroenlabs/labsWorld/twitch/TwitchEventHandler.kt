package nl.jeroenlabs.labsWorld.twitch

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.eventsub.EventSubSubscription
import com.github.twitch4j.eventsub.events.ChannelCheerEvent
import com.github.twitch4j.eventsub.events.ChannelFollowEvent
import com.github.twitch4j.eventsub.events.ChannelPointsCustomRewardRedemptionEvent
import com.github.twitch4j.eventsub.events.ChannelRaidEvent
import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent
import com.github.twitch4j.eventsub.events.ChannelSubscriptionGiftEvent
import com.github.twitch4j.eventsub.events.ChannelSubscriptionMessageEvent
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes
import nl.jeroenlabs.labsWorld.twitch.commands.CommandDispatcher
import nl.jeroenlabs.labsWorld.twitch.commands.CommandRegistry
import nl.jeroenlabs.labsWorld.twitch.redeems.BuiltInRedeemHandlers
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemDispatcher
import nl.jeroenlabs.labsWorld.twitch.redeems.RedeemHandlerRegistry
import org.bukkit.plugin.java.JavaPlugin

class TwitchEventHandler(
    private val plugin: JavaPlugin,
    private val twitchClient: TwitchClient,
    private val twitchConfigManager: TwitchConfigManager,
    private val commandRegistry: CommandRegistry,
    private val twitchClientManager: TwitchClientManager? = null,
) {
    private val commandDispatcher = CommandDispatcher(plugin, twitchClient, commandRegistry)

    private val redeemRegistry =
        RedeemHandlerRegistry().also {
            BuiltInRedeemHandlers.registerAll(it)
        }

    private val redeemDispatcher = RedeemDispatcher(plugin, twitchClient, twitchConfigManager, redeemRegistry)

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

        // Register Channel Cheer Event (Bits)
        twitchClient.eventManager.onEvent(
            ChannelCheerEvent::class.java,
            this::handleChannelCheerEvent,
        )

        // Register Channel Raid Event
        twitchClient.eventManager.onEvent(
            ChannelRaidEvent::class.java,
            this::handleChannelRaidEvent,
        )

        // Register Channel Follow Event
        twitchClient.eventManager.onEvent(
            ChannelFollowEvent::class.java,
            this::handleChannelFollowEvent,
        )

        // Register Channel Subscribe Event
        twitchClient.eventManager.onEvent(
            ChannelSubscribeEvent::class.java,
            this::handleChannelSubscribeEvent,
        )

        twitchClient.eventManager.onEvent(
            ChannelSubscriptionGiftEvent::class.java,
            this::handleChannelSubscribeEvent,
        )

        twitchClient.eventManager.onEvent(
            ChannelSubscriptionMessageEvent::class.java,
            this::handleChannelSubscribeEvent,
        )

        plugin.logger.info("Registered all Twitch event handlers")
    }

    private fun handleChannelMessageEvent(event: ChannelMessageEvent) {
        plugin.logger.info("Handling chat message from ${event.user.name}: ${event.message}")
        commandDispatcher.handle(event)
    }

    private fun handleChannelPointsCustomRewardsRedemptionEvent(event: ChannelPointsCustomRewardRedemptionEvent) {
        plugin.logger.info("Handling redeem event for reward=${event.reward.title}")
        redeemDispatcher.handle(event)
    }

    private fun handleChannelRaidEvent(event: ChannelRaidEvent) {
    }

    private fun handleChannelCheerEvent(event: ChannelCheerEvent) {
    }

    private fun handleChannelFollowEvent(event: ChannelFollowEvent) {
    }

    private fun handleChannelSubscribeEvent(event: ChannelSubscribeEvent) {
    }

    private fun handleChannelSubscribeEvent(event: ChannelSubscriptionGiftEvent) {
    }

    private fun handleChannelSubscribeEvent(event: ChannelSubscriptionMessageEvent) {
    }
}
