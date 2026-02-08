# T22: ChatSayHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/ChatSayHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/ChatSayHandlerTest.kt`
**Current coverage:** 0/5 lines (0%)
**Target coverage:** ~100%

## Why This Matters
5 uncovered lines. Simple handler but easy 100% coverage.

## Approach
- Mock `TwitchContext`, `RedeemInvocation`
- Mock `say` extension function or verify chat.sendMessage

## Tests to Write

### `@Nested class Properties`
- `key is "chat.say"`
- `runOnMainThread is false`

### `@Nested class Handle`
- `sends rendered message to chat` — message param with {user}, verify `say()` called with substituted text
- `does nothing when message param is null` — no crash, no chat message
- `does nothing when message param is not a String` — params["message"] = 123
