# Idea E01: NPC Currency

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `E01`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/e01-npc-currency`     |

**Goal:**
Introduce a virtual currency system for NPCs. Viewers earn coins from duels, quests, chat activity, and events. Coins can be spent on upgrades, cosmetics, and interactions. Foundation for the in-game economy.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   ├── VillagerNpcCurrencyService.kt      # CREATE — currency management
│   └── VillagerNpcKeys.kt                 # MODIFY — add currency PDC key
├── twitch/commands/lw/
│   └── BalanceSubcommand.kt               # CREATE — check balance command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcCurrencyService`:
   - `getBalance(userId): Int` — read balance from storage
   - `addCoins(userId, amount, reason)` — add coins with audit log
   - `removeCoins(userId, amount): Boolean` — deduct coins (returns false if insufficient)
   - `transferCoins(fromId, toId, amount): Boolean` — peer-to-peer transfer
   - Store balances in YAML: `npc-currency.yml`
2. Earning rates (configurable):
   - Chat message: 1 coin (capped at 10/minute)
   - Duel win: 50 coins
   - Duel participation: 10 coins
   - Quest completion: varies
   - Survival game win: 100 coins
3. `!lw balance` — show current balance
4. `!lw pay @viewer <amount>` — transfer coins to another viewer
5. Integrate earning hooks into existing services (duel, chat)
6. Announce large earnings in Twitch chat

**Paper API Used:**
- YAML persistence via `YamlConfiguration` — balance storage
- `PersistentDataContainer` — cache on NPC entity
- Integration hooks into existing service classes

**Acceptance Criteria:**

- [ ] Currency balances persist across server restarts
- [ ] `!lw balance` shows viewer's coin count
- [ ] Coins earned from duels and chat activity
- [ ] `!lw pay @viewer 50` transfers coins
- [ ] Earning rates are configurable
- [ ] Insufficient funds properly handled
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
