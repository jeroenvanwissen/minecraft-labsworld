# Idea G02: NPC Gift Giving

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `G02`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | E01 (NPC Currency, optional)   |
| **Branch**       | `feature/g02-npc-gift-giving`  |

**Goal:**
Viewers can give virtual gifts to other viewers' NPCs via `!lw gift @viewer <item>`. The giving NPC walks to the receiving NPC and drops the gift item. Creates social interactions between viewers.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcGiftService.kt          # CREATE — gift delivery logic
├── twitch/commands/lw/
│   └── GiftSubcommand.kt                  # CREATE — gift command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcGiftService`:
   - `deliverGift(giverNpc, receiverNpc, giftType)`:
     - Giver NPC pathfinds to receiver NPC
     - When within 2 blocks: drop an item at receiver's feet
     - Show heart particles between both NPCs
     - Show chat bubble on giver: "Here you go, @receiver!"
     - Show chat bubble on receiver: "Thanks, @giver!"
   - Available gift items (cosmetic only):
     - flower (poppy), cake, diamond, cookie, book, golden_apple
   - Optionally costs coins (if currency system exists)
   - Cooldown: 1 gift per viewer per 5 minutes
2. `!lw gift @viewer flower` — send a gift
3. Announce in Twitch chat: "@giver gave @receiver a flower!"
4. Gift item despawns after 10 seconds (cosmetic only)
5. Both NPCs must be spawned for gifting to work

**Paper API Used:**
- `Mob.getPathfinder().moveTo(entity)` — walk to receiver
- `World.dropItemNaturally(location, itemStack)` — drop gift item
- `World.spawnParticle(Particle.HEART)` — love particles
- Existing `ChatBubbleService` — display messages
- `Location.distance()` — proximity check
- `BukkitRunnable` — delivery sequence
- `Item.setPickupDelay()` + `Item.setTicksLived()` — auto-despawn

**Acceptance Criteria:**

- [ ] `!lw gift @viewer flower` initiates gift delivery
- [ ] Giver NPC walks to receiver NPC
- [ ] Gift item drops at receiver's location
- [ ] Heart particles and chat bubbles display
- [ ] Gift announced in Twitch chat
- [ ] Cooldown prevents spam
- [ ] Both NPCs must exist
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
