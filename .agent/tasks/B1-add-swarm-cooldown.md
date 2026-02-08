# Task B1: Add Cooldown for Swarm and Attack Services

| Field            | Value        |
| ---------------- | ------------ |
| **ID**           | `B1`         |
| **Status**       | `[ ]`        |
| **Priority**     | Medium       |
| **Dependencies** | None         |
| **Branch**       | `fix/b1-swarm-cooldown` |

**Goal:**
Prevent overlapping swarm and attack operations by tracking active state and rejecting new requests while one is in progress.

**Scope:**

```
plugin/src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/VillagerNpcSwarmService.kt          # MODIFY — add isActive check
└── npc/VillagerNpcAttackService.kt         # MODIFY — add isActive check
```

**Implementation:**

1. Add an `isActive` property to both `VillagerNpcSwarmService` and `VillagerNpcAttackService`
2. Check `isActive` at the start of `startSwarm()` / `startAttack()` and return `Result.failure()` if active
3. Log when a request is rejected due to an active operation

**Acceptance Criteria:**

- [ ] Starting a swarm while one is active returns a failure result
- [ ] Starting an attack while one is active returns a failure result
- [ ] Active state is cleared when the operation completes
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
