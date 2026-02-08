# T10: TwitchEventHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/TwitchEventHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/TwitchEventHandlerTest.kt`
**Current coverage:** 0/35 lines (0%)
**Target coverage:** ~70%+

## Why This Matters
35 uncovered lines. Wires up all Twitch event listeners — ensuring correct registration and delegation is important.

## Approach
- Mock `LabsWorld`, `TwitchClient`, `TwitchConfigManager`, `TwitchClientManager`
- Mock `twitchClient.eventManager`, `twitchClient.chat`, `twitchClient.eventSocket`
- Verify event handlers are registered and delegate correctly

## Tests to Write

### `@Nested class RegisterEventHandlers`
- `registers ChannelMessageEvent listener` — verify `eventManager.onEvent(ChannelMessageEvent::class.java, ...)` called
- `registers ChannelPointsCustomRewardRedemptionEvent listener` — verify `eventManager.onEvent(...)` called
- `registers EventSocket subscription` — verify `eventSocket.register(...)` called
- `does nothing when channelName is null` — verify no registration

### `@Nested class HandleChannelMessageEvent`
- `delegates to commandDispatcher.handle` — create mock event, call handler, verify dispatch
- `logs message info` — verify plugin logger called

### `@Nested class HandleChannelPointsRedeemEvent`
- `delegates to redeemDispatcher.handle` — create mock event, call handler, verify dispatch
- `logs reward title` — verify plugin logger called

## Notes
- The `commandDispatcher` and `redeemDispatcher` are created in the constructor. Use reflection to verify they exist, or verify behavior by triggering the event callbacks.
- `SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD` is a Twitch4J static — may need `mockkStatic` or just verify the `eventSocket.register` is called with any argument.
