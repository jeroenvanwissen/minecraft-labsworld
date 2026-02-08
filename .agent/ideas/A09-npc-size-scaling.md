# Idea A09: NPC Size Scaling

| Field            | Value                            |
| ---------------- | -------------------------------- |
| **ID**           | `A09`                            |
| **Status**       | `[ ]`                            |
| **Dependencies** | None                             |
| **Branch**       | `feature/a09-npc-size-scaling`   |

**Goal:**
Scale NPC size based on subscriber tier, level, or duel wins. Tier 3 subs get a noticeably larger NPC. Makes high-tier supporters visually stand out in the city.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcManager.kt              # MODIFY — apply scale on spawn
└── npc/
    └── VillagerNpcKeys.kt                 # MODIFY — add scale PDC key
```

**Implementation:**

1. Add a `npc_scale` PDC key to `VillagerNpcKeys`
2. Use the `Attributable.getAttribute(Attribute.GENERIC_SCALE)` to set NPC scale
3. Define scale tiers:
   - Default: 1.0 (normal size)
   - Subscriber: 1.1
   - Tier 2 sub: 1.2
   - Tier 3 sub: 1.3
   - Or: scale = 1.0 + (level * 0.02), capped at 1.5
4. Apply scale when NPC is created or respawned
5. Optionally allow manual scale via broadcaster command `!lw scale @user 1.3`

**Paper API Used:**
- `Attributable.getAttribute(Attribute.GENERIC_SCALE)` — entity scale attribute (1.20.5+)
- `AttributeInstance.setBaseValue(double)` — set the scale value
- `PersistentDataContainer` — persist scale preference

**Acceptance Criteria:**

- [ ] NPC size varies based on subscriber tier or configured scale
- [ ] Scale is visually noticeable but not game-breaking (max 1.5x)
- [ ] Scale persists across NPC respawns
- [ ] Broadcaster can manually set scale for any NPC
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
