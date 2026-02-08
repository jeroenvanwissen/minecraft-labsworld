# Minecraft Twitch City

A Minecraft mod that enables Twitch viewers to interact with a Minecraft world by spawning and controlling NPCs through Twitch chat commands and channel point redemptions.

## Overview

This project creates an interactive Minecraft experience where Twitch viewers become part of the game. Viewers can spawn their own NPCs in a custom-built city, control them through chat commands, and participate in various game modes. The entire project is being developed live on Twitch, building both the mod and the Minecraft world together with the community.

## Features

### Current Features

- Twitch integration for channel point redemptions
- Viewer NPC spawning system
- Custom spawn point block placement
- Basic NPC management (one NPC per viewer)

### Planned Features

- **NPC Commands**: Chat-based NPC control and interaction
- **Duel System**: NPC vs NPC combat with challenge/accept mechanics
- **Capture the Flag**: Team-based gameplay mode
- **Combat System**: Damage mechanics for NPCs (player damage, TNT, etc.)

## Project Structure

```
minecraft-labsworld/
├── plugin/          # Core Minecraft mod source code
├── world/           # Custom Minecraft world files
└── .github/         # CI/CD workflows and automation
```

## Development Phases

### Phase 0: Foundation ✓

- Clean up experimental code
- Update dependencies to latest versions
- Ensure compilation stability

### Phase 1: Core Functionality (In Progress)

- Set up Minecraft server with required mods
- Build Twitch connection and channel point redemption handling
- Implement basic NPC spawning mechanics
- Develop custom spawn point marker block

### Phase 2: Interactive Commands

- Implement Twitch chat command system
- Add NPC control and interaction commands

### Phase 3: Game Modes

- Duel system with challenge/accept mechanics
- Random hit/miss combat (10 hearts per NPC)
- Capture the Flag mode
- Additional game modes TBD

## Viewer Interaction

### NPC Spawning

- Viewers redeem channel points to spawn their NPC
- Each viewer can have only one NPC at a time
- NPCs display the viewer's Twitch username
- Spawn location: Center of the city

### Chat Commands

- `!duel @viewer` - Challenge another viewer's NPC to a duel
- More commands coming soon!

## Development Roadmap

- [ ] NPC damage system implementation
- [ ] Duel game mode with accept/decline mechanics
- [ ] Combat system with random hits/misses
- [ ] Capture the Flag game mode
- [ ] Respawn mechanics for defeated NPCs
- [ ] Additional viewer interaction commands

## Development

This project is being built live on Twitch! Join the stream to watch development in real-time and contribute ideas to the project.

### Requirements

- Minecraft server (version TBD)
- Required mods (list TBD)
- Twitch developer credentials
- Java Development Kit

### Building

Instructions coming soon as the project stabilizes.

## Contributing

This is a live-streamed development project. Contributions and ideas are welcome through:

- Twitch chat during development streams
- GitHub issues for bug reports
- Pull requests for improvements

## License

License information TBD

## Acknowledgments

Built live on Twitch with input and ideas from the amazing community!
