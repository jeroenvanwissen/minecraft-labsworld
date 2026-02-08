# Idea B10: Bounty System

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `B10`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | E01 (NPC Currency, optional)   |
| **Branch**       | `feature/b10-bounty-system`    |

**Goal:**
Viewers can place bounties on other NPCs via `!lw bounty @target <amount>`. Any NPC that eliminates the target in a duel collects the bounty. Creates rivalry and drama between viewers. Most-wanted NPC gets a special glow.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcBountyService.kt        # CREATE — bounty management
├── twitch/commands/lw/
│   ├── BountySubcommand.kt                # CREATE — place/check bounties
│   └── BountyListSubcommand.kt            # CREATE — list active bounties
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommands
```

**Implementation:**

1. Create `VillagerNpcBountyService`:
   - Track active bounties: Map of target user ID → list of (placer, amount) pairs
   - Total bounty = sum of all amounts on a target
   - When target NPC is eliminated in a duel, bounty is collected by the winner
   - Announce bounty collection in Twitch chat
   - NPC with highest total bounty gets red glow outline
   - Bounties expire after configurable time (default: 1 hour)
2. `!lw bounty @viewer 100` — place a bounty (requires currency system or free)
3. `!lw bounty list` — show top 5 most-wanted NPCs
4. `!lw bounty check` — check bounty on own NPC
5. Persist active bounties in YAML

**Paper API Used:**
- `Entity.setGlowing(true)` — highlight most-wanted NPC
- `Scoreboard Team` with `ChatColor.RED` — red glow for bounty target
- `PersistentDataContainer` — store bounty metadata
- Adventure API — formatted bounty announcements
- YAML persistence for bounty state

**Acceptance Criteria:**

- [ ] `!lw bounty @user 100` places a bounty on a target NPC
- [ ] Bounty stacks from multiple viewers
- [ ] Most-wanted NPC has a red glow
- [ ] Eliminating a bounty target in a duel awards the bounty
- [ ] `!lw bounty list` shows top bounties
- [ ] Bounties expire after configured time
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
