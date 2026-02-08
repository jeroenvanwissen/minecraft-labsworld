# Idea C09: Enhanced Chat Bubbles

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `C09`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/c09-enhanced-chat-bubbles`|

**Goal:**
Upgrade the existing chat bubble system to style bubbles differently based on viewer status. Subscribers get colored bubbles, VIPs get special frames, and mods get highlighted bubbles. Adds visual hierarchy to NPC communication.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── ChatBubbleService.kt              # MODIFY — add styling by tier
```

**Implementation:**

1. Modify `ChatBubbleService.showBubble()` to accept user role information
2. Style bubbles by tier using Adventure API text components:
   - **Everyone**: White text, default background (current behavior)
   - **Subscriber**: Green text with subtle green background
   - **VIP**: Gold text with star prefix
   - **Moderator**: Blue text with sword prefix
   - **Broadcaster**: Red text with crown prefix
3. Use `TextDisplay` background color property for colored backgrounds:
   - `TextDisplay.setBackgroundColor(Color)` — set bubble background
4. Add subscriber badge indicator (text prefix like icons)
5. Optionally show sub tier number: [T1], [T2], [T3]
6. Configurable colors and prefixes per tier in config

**Paper API Used:**
- `TextDisplay.setBackgroundColor(Color)` — colored bubble background
- `TextDisplay.text(Component)` — styled text with Adventure API
- `Component.text().color(TextColor)` — colored text
- `Component.text().decorate(TextDecoration)` — bold, italic for emphasis
- Existing `ChatBubbleService` infrastructure

**Acceptance Criteria:**

- [ ] Regular viewers get default white bubbles
- [ ] Subscribers get green-tinted bubbles
- [ ] VIPs get gold-colored text with prefix
- [ ] Moderators get blue-colored text with prefix
- [ ] Broadcaster gets red-colored text with prefix
- [ ] Colors and prefixes are configurable
- [ ] Backward-compatible with existing bubble behavior
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
