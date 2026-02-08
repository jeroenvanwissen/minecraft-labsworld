# T08: VillagerNpcManager Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcManager.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcManagerTest.kt`
**Current coverage:** 0/41 lines (0%)
**Target coverage:** ~60%+

## Why This Matters
41 uncovered lines. NPC creation and unique name generation logic.

## Approach
- Mock `JavaPlugin`, `Server`, `World`, `Location`
- `spawnEntity` returns a mock `Villager` — verify PDC tags set, AI/invulnerability config
- `generateUniqueName` iterates worlds — mock `server.worlds` with villagers that have custom names

## Tests to Write

### `@Nested class CreateLinkedNpc`
- `creates villager with twitch user ID in PDC` — verify `persistentDataContainer.set()` called with twitchUserId key
- `sets custom_npc tag` — verify PDC has customNpcTag
- `sets display name` — verify `customName(Component.text(name))`
- `sets profession when provided` — verify `profession = Villager.Profession.FARMER`
- `configures AI, invulnerability, silent, removeWhenFarAway`

### `@Nested class GenerateUniqueName`
(Tested through `createLinkedNpc`)
- `returns base name when not taken` — no existing NPCs, name stays "testuser"
- `appends #1 when base name is taken` — one existing NPC with same name
- `appends #2 when #1 is also taken` — two existing NPCs

### `@Nested class IsCustomNpc`
- `returns true for NPC with custom_npc tag`
- `returns false for regular villager`

### `@Nested class GetAllCustomNpcs`
- `returns only villagers with custom_npc tag`
- `searches across all worlds`
- `returns empty list when no custom NPCs exist`

## Notes
- `spawnEntity` is called on `location.world` — mock `World.spawnEntity()` to return a mock Villager.
- `PlainTextComponentSerializer` may need to be available — Paper API test dependency should provide it.
