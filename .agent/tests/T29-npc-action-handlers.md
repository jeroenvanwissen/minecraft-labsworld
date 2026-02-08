# T29: NPC Action Handlers Tests

**Source files:**
- `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/VillagerNpcSpawnActionHandler.kt` (9 lines, 22.2%)
- `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/VillagerNpcSwarmActionHandler.kt` (14 lines, 14.3%)
- `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/VillagerNpcAttackActionHandler.kt` (16 lines, 12.5%)

**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/NpcActionHandlersTest.kt`
**Combined uncovered:** ~30 lines
**Target coverage:** ~80%+

## Why This Matters
Three small related handlers — testing them together reduces boilerplate.

## Approach
- Mock `TwitchContext` with mocked `LabsWorld`
- Mock `ActionUtils.resolveTargetPlayer`
- Mock `ActionInvocation`

## Tests to Write

### `@Nested class VillagerNpcSpawnActionHandler`
- `type is "npc.spawn"`
- `errors when no spawn point placed` — pickNpcSpawnPointSpawnLocation returns null
- `calls ensureNpcAtSpawnPoint with user info` — verify userId, userName
- `sends success message to chat`
- `propagates errors`

### `@Nested class VillagerNpcSwarmActionHandler`
- `type is "npc.swarm_player"`
- `returns early when no target player`
- `calls startSwarmAllNpcs with correct params` — duration from params
- `sends count message to chat on success`
- `sends "No Twitch NPCs found" when count is 0`
- `coerces duration to at least 1`

### `@Nested class VillagerNpcAttackActionHandler`
- `type is "npc.attack_player"`
- `returns early when no target player`
- `calls startAttackAllNpcs with correct params` — duration, hearts_per_hit
- `sends count message to chat on success`
- `sends "No Twitch NPCs found" when count is 0`
- `coerces duration and hearts_per_hit to at least 1/0.1`
