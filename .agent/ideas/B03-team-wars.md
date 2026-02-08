# Idea B03: Team Wars

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `B03`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/b03-team-wars`        |

**Goal:**
Split all NPCs into two teams (Red vs Blue) and have them fight in a team battle. Teams are randomly assigned or viewers can pick sides via `!lw team red/blue`. Last team with surviving NPCs wins.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   ├── VillagerNpcTeamWarService.kt       # CREATE — team war game loop
│   └── VillagerNpcKeys.kt                 # MODIFY — add team PDC key
├── twitch/commands/lw/
│   ├── TeamWarSubcommand.kt               # CREATE — start command
│   └── TeamSubcommand.kt                  # CREATE — join team command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommands
```

**Implementation:**

1. Create `TeamSubcommand` — `!lw team red` or `!lw team blue` to pre-select team
2. Create `TeamWarSubcommand` — `!lw teamwar` to start (broadcaster only)
3. `VillagerNpcTeamWarService`:
   - Assign unassigned NPCs randomly to balance teams
   - Dye NPC armor: Red team = leather armor dyed red, Blue = blue
   - Teleport teams to opposite ends of a designated area
   - NPCs target and attack only enemy team members
   - Track eliminations per team
   - Eliminated NPCs: removed, respawned as spectators (no combat)
   - Team with last surviving NPC(s) wins
4. Use Scoreboard teams for glow colors (red glow vs blue glow)
5. Announce team eliminations and final result to Twitch chat

**Paper API Used:**
- `Scoreboard.registerNewTeam()` — team management with glow colors
- `Team.setColor(ChatColor.RED/BLUE)` — colored glow outlines
- `Entity.setGlowing(true)` — enable glow for team identification
- `LeatherArmorMeta.setColor(Color)` — dye armor for team colors
- `Mob.getPathfinder().moveTo(entity)` — target enemy NPCs
- `LivingEntity.damage(amount, source)` — combat

**Acceptance Criteria:**

- [ ] `!lw team red` lets viewer pre-select their team
- [ ] `!lw teamwar` starts the game with balanced teams
- [ ] NPCs wear team-colored armor and have colored glow
- [ ] NPCs only attack enemy team members
- [ ] Eliminations are tracked and announced
- [ ] Winning team is declared when all enemies are eliminated
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
