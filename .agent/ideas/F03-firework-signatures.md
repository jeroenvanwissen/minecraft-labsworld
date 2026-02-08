# Idea F03: Firework Signatures

| Field            | Value                                |
| ---------------- | ------------------------------------ |
| **ID**           | `F03`                                |
| **Status**       | `[ ]`                                |
| **Dependencies** | None                                 |
| **Branch**       | `feature/f03-firework-signatures`    |

**Goal:**
Each viewer gets a personalized firework pattern and color scheme. When their NPC triggers a firework (respawn, celebration, redeem), it uses their unique signature. Viewers can customize colors via `!lw firework <color1> <color2>`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcFireworkSignatureService.kt # CREATE — signature management
├── twitch/commands/lw/
│   └── FireworkSubcommand.kt              # CREATE — customize colors
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcFireworkSignatureService`:
   - Store per-viewer firework preferences in YAML: `npc-firework-signatures.yml`
   - Preferences: primary color, fade color, shape (ball/star/burst/creeper/large_ball)
   - Default: auto-generate colors based on username hash (deterministic)
   - `createSignatureFirework(userId, location)` — spawn with viewer's preferences
   - `setColors(userId, primary, fade)` — update preferences
2. `!lw firework red blue` — set primary and fade colors
3. `!lw firework shape star` — set firework shape
4. `!lw firework show` — launch a preview firework
5. Integrate into existing firework spawning code (action handler, respawn, celebrations)
6. Available colors: red, blue, green, yellow, orange, purple, white, cyan, pink

**Paper API Used:**
- `FireworkMeta.addEffect(FireworkEffect)` — set firework effect
- `FireworkEffect.builder().withColor(Color).withFade(Color).with(Type).build()`
- `FireworkEffect.Type` — BALL, BALL_LARGE, BURST, CREEPER, STAR
- `Color.fromRGB()` — custom colors
- `World.spawn(Firework.class)` — launch firework
- `FireworkMeta.setPower(int)` — flight height
- YAML persistence for preferences

**Acceptance Criteria:**

- [ ] Each viewer gets a default firework signature based on username
- [ ] `!lw firework red blue` customizes colors
- [ ] `!lw firework shape star` customizes shape
- [ ] `!lw firework show` launches a preview
- [ ] Signature used for all firework events (respawn, celebration, redeems)
- [ ] Preferences persist across sessions
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
