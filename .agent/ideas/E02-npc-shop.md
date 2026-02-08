# Idea E02: NPC Shop

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `E02`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | E01 (NPC Currency)             |
| **Branch**       | `feature/e02-npc-shop`         |

**Goal:**
A chat-based shop where viewers spend coins to buy upgrades for their NPC. Items include armor, weapons, effects, pets, and cosmetics. Encourages engagement to earn coins and gives meaningful progression.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcShopService.kt          # CREATE — shop catalog & purchase logic
├── twitch/commands/lw/
│   ├── ShopSubcommand.kt                  # CREATE — browse/buy commands
│   └── ShopListSubcommand.kt              # CREATE — list catalog
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommands
```

**Implementation:**

1. Create `VillagerNpcShopService`:
   - Define shop catalog in config YAML:
     ```yaml
     shop:
       items:
         - id: iron_sword
           name: "Iron Sword"
           price: 100
           type: equipment
           slot: hand
           material: IRON_SWORD
         - id: speed_boost
           name: "Speed Boost (5 min)"
           price: 50
           type: effect
           effect: SPEED
           duration: 6000
     ```
   - `listItems()` — return catalog
   - `buyItem(userId, itemId)` — deduct coins, apply item
   - Apply purchases: equipment to NPC, effects as potion effects
2. `!lw shop` — list available items with prices
3. `!lw shop buy <id>` — purchase an item
4. Categories: equipment, effects, cosmetics, pets
5. Configurable catalog — add/remove items via config
6. Track purchases per viewer to prevent duplicates (for equipment)

**Paper API Used:**
- `EntityEquipment.setHelmet/setItemInMainHand()` — equipment
- `LivingEntity.addPotionEffect()` — temporary effects
- YAML config for catalog definition
- `VillagerNpcCurrencyService` — coin deduction
- Adventure API — formatted shop display

**Acceptance Criteria:**

- [ ] `!lw shop` lists available items with prices
- [ ] `!lw shop buy iron_sword` purchases and equips item
- [ ] Coins are deducted from balance
- [ ] Insufficient funds shows error message
- [ ] Shop catalog is configurable via YAML
- [ ] Equipment persists on NPC
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
