# Idea G03: NPC Alliances

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `G03`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/g03-npc-alliances`    |

**Goal:**
Viewers can form alliances (groups of NPCs) that share a team color, stick together, and provide benefits in team events. Alliance members get matching glow colors and proximity bonuses. Creates community within the community.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcAllianceService.kt      # CREATE — alliance management
├── twitch/commands/lw/
│   ├── AllianceSubcommand.kt              # CREATE — create/join/leave alliance
│   └── AllianceListSubcommand.kt          # CREATE — list alliances
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommands
```

**Implementation:**

1. Create `VillagerNpcAllianceService`:
   - Alliance data: name, color, leader (creator), members list
   - Store alliances in YAML: `npc-alliances.yml`
   - Max alliance size: configurable (default 5)
   - Max alliances: configurable (default 10)
   - Alliance perks:
     - Members' NPCs get matching glow color (Scoreboard team)
     - NPCs pathfind to stay near alliance members when idle
     - Alliance members don't target each other in team wars
   - Invite system: `!lw alliance invite @viewer` → viewer must accept
2. Commands:
   - `!lw alliance create <name>` — create (becomes leader)
   - `!lw alliance invite @viewer` — invite member (leader only)
   - `!lw alliance accept` — accept pending invite
   - `!lw alliance leave` — leave current alliance
   - `!lw alliance list` — list all alliances
   - `!lw alliance info` — show own alliance details
3. Alliance NPCs walk together when idle (cluster behavior)

**Paper API Used:**
- `Scoreboard.registerNewTeam()` — team for glow color
- `Team.setColor(NamedTextColor)` — colored glow
- `Entity.setGlowing(true)` — enable glow
- `Mob.getPathfinder().moveTo(entity)` — proximity clustering
- YAML persistence for alliance data
- Adventure API — formatted alliance info

**Acceptance Criteria:**

- [ ] `!lw alliance create <name>` creates an alliance
- [ ] `!lw alliance invite @viewer` sends an invite
- [ ] Members get matching glow color
- [ ] Alliance NPCs cluster together when idle
- [ ] `!lw alliance list` shows all alliances
- [ ] One alliance per viewer
- [ ] Alliance data persists across restarts
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
