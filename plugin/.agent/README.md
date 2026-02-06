# Agent Environment for LabsWorld Plugin Refactoring

This folder contains everything needed to run refactoring tasks with GitHub Copilot.

## Contents

| File                                     | Purpose                                        |
| ---------------------------------------- | ---------------------------------------------- |
| [TASKS.md](TASKS.md)                     | All refactoring tasks with acceptance criteria |
| [HOWTO.md](HOWTO.md)                     | Setup guide and usage                          |
| [Dockerfile](Dockerfile)                 | Container for build verification               |
| [docker-compose.yml](docker-compose.yml) | Easy container orchestration                   |
| [scripts/](scripts/)                     | Helper scripts for verification                |

## Quick Start

### Option 1: VS Code + GitHub Copilot (Recommended)

This is what you're using right now! Simply:

1. Open `.agent/TASKS.md`
2. Ask Copilot Chat: _"Execute task A1 from this file"_
3. Review the changes Copilot makes
4. Run verification: `./.agent/scripts/verify.sh`
5. Commit and create PR

### Option 2: Docker (for isolated builds)

```bash
cd .agent
cp .env.example .env
# Edit .env with GITHUB_TOKEN

docker-compose build
docker-compose run --rm agent

# Inside container - verify builds
./gradlew compileKotlin
./gradlew shadowJar
```

## Workflow

```
1. Pick next task from TASKS.md
2. Create branch: git checkout -b refactor/<task-id>
3. Ask Copilot Chat to implement the task
4. Run ./.agent/scripts/verify.sh
5. Commit and push
6. Create PR with gh pr create
```

## Security

- Git credentials passed via environment (not in image)
- Workspace mounted read-write but isolated
- Never commit `.env` files
