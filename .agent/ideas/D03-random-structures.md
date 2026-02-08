# Idea D03: Random Structure Spawning

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `D03`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/d03-random-structures`    |

**Goal:**
Viewers can redeem channel points to spawn small decorative structures near their NPC. Structures include a market stall, a park bench, a flower garden, a fountain, or a small tower. Adds viewer-built personality to the city.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/actions/handlers/
│   └── SpawnStructureActionHandler.kt     # CREATE — structure placement
├── twitch/actions/
│   └── ActionExecutor.kt                  # MODIFY — register handler
```

**Implementation:**

1. Create `SpawnStructureActionHandler`:
   - Define small structures as block palettes (3x3x3 to 5x5x5 max):
     - **market_stall**: Oak logs frame, wool roof, fence posts
     - **park_bench**: Oak stairs + signs
     - **flower_garden**: 3x3 grass with flowers
     - **fountain**: Stone brick ring, water source in center
     - **tower**: 3x3 cobblestone, 5 blocks tall, ladder
   - Place structure near the viewer's NPC (find safe flat area)
   - Check for safe placement: no overlapping existing structures or buildings
   - Mark structure blocks with PDC to track ownership
2. Parameters: `structure_type`, `target_player` (optional)
3. Register as `world.spawn_structure` action type
4. Limit: 1 structure per viewer to prevent world clutter
5. `!lw demolish` — remove viewer's structure (broadcaster or owner)

**Paper API Used:**
- `Block.setType(Material)` — place blocks
- `Block.setBlockData(BlockData)` — set orientations for stairs, signs, etc.
- `BlockData` subclasses — directional blocks, waterlogged
- `PersistentDataContainer` on `TileState` — mark ownership
- `Location.getBlock()` — access world blocks
- Surface detection: scan downward for first solid block

**Acceptance Criteria:**

- [ ] At least 5 distinct structure types available
- [ ] Structures place near the viewer's NPC location
- [ ] Safe placement check prevents overlapping
- [ ] One structure per viewer limit
- [ ] Structures are tagged with owner for cleanup
- [ ] `!lw demolish` removes viewer's structure
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
