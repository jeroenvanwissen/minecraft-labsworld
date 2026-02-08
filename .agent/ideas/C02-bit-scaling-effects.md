# Idea C02: Bit-Scaled Effects

| Field            | Value                                |
| ---------------- | ------------------------------------ |
| **ID**           | `C02`                                |
| **Status**       | `[ ]`                                |
| **Dependencies** | None                                 |
| **Branch**       | `feature/c02-bit-scaling-effects`    |

**Goal:**
Twitch cheers (bits) trigger in-game effects that scale with the amount cheered. Small cheers = small effects, large cheers = dramatic effects. For example, 100 bits = 1 firework, 1000 bits = firework show, 10000 bits = TNT rain.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/
│   └── TwitchEventHandler.kt             # MODIFY — listen for cheer events
├── twitch/actions/handlers/
│   └── BitScaleActionHandler.kt           # CREATE — scaled effect logic
└── twitch/actions/
    └── ActionExecutor.kt                  # MODIFY — register handler
```

**Implementation:**

1. Listen for Twitch cheer events via EventSub or IRC tags
2. Create `BitScaleActionHandler` with tiered effects:
   - **1-99 bits**: Spawn particles around cheerer's NPC
   - **100-499 bits**: Firework at NPC location + chat announcement
   - **500-999 bits**: Firework show (5 fireworks) + spawn friendly mobs
   - **1000-4999 bits**: Mega firework show + heal all NPCs + sound effect
   - **5000+ bits**: TNT rain (cosmetic) + lightning strikes + all NPCs celebrate
3. All tier thresholds configurable in config YAML
4. Announce cheer with tier name in Twitch chat: "MEGA CHEER from @user!"
5. Can be disabled per tier in config

**Paper API Used:**
- `World.spawn(Firework.class)` — fireworks scaled by tier
- `World.spawnParticle()` — particle effects
- `World.strikeLightningEffect()` — cosmetic lightning (no damage)
- `World.playSound()` — celebration sounds
- `World.spawn(EntityType)` — friendly mobs
- Existing `FireworksActionHandler` can be reused for firework spawning

**Acceptance Criteria:**

- [ ] Cheers trigger in-game effects scaled to bit amount
- [ ] At least 5 tiers with distinct effects
- [ ] Tiers and thresholds are configurable
- [ ] Effect plays at the cheerer's NPC location (or center if no NPC)
- [ ] Cheer announcement sent to Twitch chat
- [ ] Individual tiers can be enabled/disabled
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
