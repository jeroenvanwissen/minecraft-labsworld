# Idea A02: NPC Armor & Weapons

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `A02`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/a02-npc-armor`        |

**Goal:**
Let viewers equip their NPC with armor and weapons via `!lw equip <slot> <item>`. NPCs with diamond armor look more impressive than naked villagers, and it adds visual progression.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcEquipmentService.kt     # CREATE — equipment management
├── twitch/commands/lw/
│   └── EquipSubcommand.kt                 # CREATE — parse & apply equipment
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcEquipmentService` with methods to set equipment by slot
2. Create `EquipSubcommand` that parses slot (head, chest, legs, feet, hand) and material
3. Build `ItemStack` from material name and apply to the villager's `EntityEquipment`
4. Use `EntityEquipment.setHelmet()`, `setChestplate()`, etc.
5. Optionally store equipped items in PDC/YAML for persistence across respawns
6. Whitelist allowed items to prevent overpowered or nonsensical gear

**Paper API Used:**
- `LivingEntity.getEquipment()` — access equipment slots
- `EntityEquipment.setHelmet/setChestplate/setLeggings/setBoots/setItemInMainHand()`
- `Material` enum — DIAMOND_HELMET, IRON_SWORD, etc.
- `ItemStack(Material, amount)`

**Acceptance Criteria:**

- [ ] `!lw equip head diamond_helmet` puts a diamond helmet on the NPC
- [ ] `!lw equip hand diamond_sword` gives the NPC a diamond sword
- [ ] Invalid slot or material returns helpful error
- [ ] Equipment visually renders on the villager in-game
- [ ] Equipment persists across NPC respawns
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
