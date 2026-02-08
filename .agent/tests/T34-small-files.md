# T34: Small/Interface Files (Pick-up Tasks)

These files are small (1-7 lines each) and contribute minimal coverage, but can be quickly tested to pad the overall number. Only tackle these if needed to reach the 75% target after completing higher-priority tasks.

## Files

### VillagerNpcKeys.kt (0/6 lines)
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcKeysTest.kt`
- Requires mocking `Villager.persistentDataContainer` and `Plugin`
- Tests:
  - `customNpcTag returns NamespacedKey with correct name`
  - `twitchUserId returns NamespacedKey with correct name`
  - `isCustomNpc returns true when PDC has tag`
  - `isCustomNpc returns false when PDC missing tag`
  - `isLinkedVillagerNpc returns true/false based on PDC`
  - `getLinkedUserId returns stored string`
  - `isLinkedToUser returns true for matching userId`

### ChatBubbleService.kt (1/39 lines, 2.6%)
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/npc/ChatBubbleServiceTest.kt`
- Heavy Bukkit dependency (TextDisplay, BukkitScheduler)
- Tests:
  - `creates TextDisplay when player is nearby`
  - `removes display when villager becomes invalid`
  - `removes display after TTL expires`
  - `does not create display when no players nearby`
  - `moves display to follow villager`

### LwSubcommand.kt + helpers (0/3 lines)
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/LwSubcommandTest.kt`
- `sanitizeTwitchName strips @ prefix`
- `sanitizeTwitchName strips non-alphanumeric chars`
- `sanitizeTwitchName preserves underscores`

### HelpSubcommand.kt (0/4 lines)
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/HelpSubcommandTest.kt`
- `name is "help"`
- `replies with command list` — verify replyMention called

### LwSubcommands.kt (0/4 lines)
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/LwSubcommandsTest.kt`
- `all contains HelpSubcommand, DuelSubcommand, ReloadSubcommand`
- `all has expected size`

### RedeemHandlers.kt (0/7 lines)
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/RedeemHandlersTest.kt`
- `all contains all expected handlers`
- `all handlers have unique keys`

### TwitchContext.kt + say extension (0/3 lines)
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/TwitchContextTest.kt`
- `say sends message to configured channel` — mock chat.sendMessage
- `say does nothing when channelName is null`
- `say swallows exceptions from sendMessage`

### LabsWorldPaperCommand.kt (0/5 lines)
Covered in T09 alongside LabsWorldCommand.

### Command.kt / CommandType / Permission enums (0/1-5 lines each)
Already implicitly tested through CommandDispatcherTest. No dedicated test needed.
