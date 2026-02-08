# Idea F04: NPC Banners

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `F04`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/f04-npc-banners`      |

**Goal:**
NPCs carry custom-colored banners on their head, acting as a personal flag. Viewers choose their banner color and pattern via `!lw banner <color> [pattern]`. Banners are visible from a distance and help identify NPCs.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcBannerService.kt        # CREATE — banner management
├── twitch/commands/lw/
│   └── BannerSubcommand.kt                # CREATE — customize banner
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcBannerService`:
   - Create banner ItemStack with viewer's chosen color and pattern
   - Apply to NPC's helmet slot via `EntityEquipment.setHelmet()`
   - Store banner preferences in YAML per viewer
   - Available base colors: all 16 DyeColor values
   - Available patterns (simplified set):
     - `stripe` — vertical stripe
     - `cross` — diagonal cross
     - `border` — border pattern
     - `gradient` — gradient effect
     - `skull` — creeper/skull pattern
   - Combine base color + pattern color for two-tone banners
2. `!lw banner red` — solid red banner
3. `!lw banner red stripe blue` — red banner with blue stripe
4. `!lw banner off` — remove banner
5. Banner persists across NPC respawns

**Paper API Used:**
- `ItemStack(Material.RED_BANNER)` — create banner item
- `BannerMeta.addPattern(Pattern)` — add patterns
- `Pattern(DyeColor, PatternType)` — define pattern
- `PatternType` enum — STRIPE_CENTER, CROSS, BORDER, GRADIENT_UP, SKULL
- `DyeColor` enum — all 16 colors
- `EntityEquipment.setHelmet(bannerItem)` — wear on head
- YAML persistence for preferences

**Acceptance Criteria:**

- [ ] `!lw banner red` sets a solid red banner on NPC's head
- [ ] `!lw banner red stripe blue` creates a two-tone banner
- [ ] `!lw banner off` removes the banner
- [ ] At least 5 pattern types available
- [ ] All 16 base colors supported
- [ ] Banner persists across NPC respawns
- [ ] Banner visually renders on the villager
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
