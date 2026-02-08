# Idea D07: Lightning Roulette

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `D07`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/d07-lightning-roulette`   |

**Goal:**
A fast-paced elimination game where lightning strikes near random NPCs. Each round, one NPC gets struck and is eliminated. Think musical chairs but with lightning. Last NPC standing wins.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcLightningRouletteService.kt # CREATE — roulette game loop
├── twitch/commands/lw/
│   └── LightningRouletteSubcommand.kt     # CREATE — start command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcLightningRouletteService`:
   - Gather all linked NPCs into a circle formation
   - Each round (every 5 seconds):
     - Dramatic build-up: rapid lightning effect jumps between NPCs (cosmetic)
     - Lightning "spins" between NPCs like a roulette wheel (visual flashes)
     - Random NPC selected for elimination
     - Full lightning strike on selected NPC
     - Eliminated NPC removed with explosion particles
     - Surviving NPCs rearrange into smaller circle
   - Continue until one NPC remains
   - Winner celebration: fireworks + glow + chat announcement
2. Build-up effect: spawn `Particle.ELECTRIC_SPARK` cycling between NPCs
3. Boss bar shows: "Lightning Roulette — X NPCs remaining"
4. Announce each elimination: "Lightning struck @username! X remain."

**Paper API Used:**
- `World.strikeLightning(location)` — real lightning strike for elimination
- `World.strikeLightningEffect(location)` — cosmetic lightning for build-up
- `World.spawnParticle(Particle.ELECTRIC_SPARK)` — electric effects
- `Entity.teleport(Location)` — arrange NPCs in circle
- `Entity.remove()` — eliminate struck NPC
- `BukkitRunnable` — round timer
- `BossBar` — remaining count display

**Acceptance Criteria:**

- [ ] `!lw lightning` starts the roulette (broadcaster/mod only)
- [ ] NPCs arranged in a circle
- [ ] Each round has a dramatic lightning build-up
- [ ] Random NPC eliminated by lightning strike
- [ ] Circle shrinks as NPCs are eliminated
- [ ] Last NPC standing wins with celebration
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
