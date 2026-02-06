#!/bin/bash
# Prepare branches for multiple tasks in sequence (respecting dependencies)
# Usage: ./scripts/run-batch.sh [PHASE]
#
# This script:
# 1. Prepares branches for each task in the phase
# 2. Opens VS Code with the task file
# 3. Waits for you to complete each task with Copilot Chat
# 4. Prompts to continue to the next task
#
# Phases:
#   A   - Foundation tasks (A1, A2, A3)
#   B   - NPC Layer tasks (B1, B2, B3)
#   C   - Deduplication tasks (C1, C2, C3)
#   D   - Twitch Layer tasks (D1-D4)
#   E   - Action System tasks (E1-E5)
#   F   - Polish tasks (F1, F2)
#   all - All phases in order

set -e

PHASE="${1:-A}"

# Colors
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

cd "$(dirname "$0")/../.."

echo -e "${CYAN}=== Batch Task Runner ===${NC}"
echo "Phase: $PHASE"
echo ""

run_with_wait() {
    local task=$1
    echo -e "${CYAN}--- Task $task ---${NC}"

    ./.agent/scripts/run-task.sh "$task" --no-open

    echo ""
    echo -e "${YELLOW}Complete task $task using Copilot Chat in VS Code.${NC}"
    echo "1. Say: 'Execute task $task from .agent/TASKS.md'"
    echo "2. Verify: ./.agent/scripts/verify.sh"
    echo "3. Commit and create PR: gh pr create --fill"
    echo ""
    read -p "Press Enter when PR is merged (or Ctrl+C to stop)..."
    echo ""
}

case $PHASE in
    A)
        echo "Phase A - Foundation (A0 → A1/A3 → A2 → A4)"
        run_with_wait "A0"  # Rename Npc* to VillagerNpc*
        run_with_wait "A1"  # Depends on A0
        run_with_wait "A3"  # Depends on A0
        run_with_wait "A2"  # Depends on A1
        run_with_wait "A4"  # Depends on A2, remove owner NPC code
        ;;
    B)
        echo "Phase B - NPC Layer (B1 depends on A1)"
        run_with_wait "B1"
        run_with_wait "B2"  # B2 depends on B1
        run_with_wait "B3"  # B3 depends on B1
        ;;
    C)
        echo "Phase C - Deduplication (independent tasks)"
        run_with_wait "C1"
        run_with_wait "C2"
        run_with_wait "C3"
        ;;
    D)
        echo "Phase D - Twitch Layer"
        run_with_wait "D1"
        run_with_wait "D2"  # D2 depends on D1
        run_with_wait "D3"  # D3 depends on D2
        run_with_wait "D4"  # D4 depends on D1
        ;;
    E)
        echo "Phase E - Action System (E1 depends on D1)"
        run_with_wait "E1"
        run_with_wait "E2"
        run_with_wait "E3"
        run_with_wait "E4"
        run_with_wait "E5"
        ;;
    F)
        echo "Phase F - Polish"
        run_with_wait "F1"  # F1 depends on B1
        run_with_wait "F2"  # F2 is independent
        ;;
    all)
        echo "Running all phases in order..."
        echo "Note: Ensure each PR is merged before dependencies are needed."
        echo ""

        # Phase A
        run_with_wait "A0"
        run_with_wait "A1"
        run_with_wait "A3"
        run_with_wait "A2"
        run_with_wait "A4"

        # Phase B (after A1)
        run_with_wait "B1"
        run_with_wait "B2"
        run_with_wait "B3"

        # Phase C (independent)
        run_with_wait "C1"
        run_with_wait "C2"
        run_with_wait "C3"

        # Phase D
        run_with_wait "D1"
        run_with_wait "D2"
        run_with_wait "D3"
        run_with_wait "D4"

        # Phase E (after D1)
        run_with_wait "E1"
        run_with_wait "E2"
        run_with_wait "E3"
        run_with_wait "E4"
        run_with_wait "E5"

        # Phase F
        run_with_wait "F1"
        run_with_wait "F2"
        ;;
    *)
        echo "Unknown phase: $PHASE"
        echo "Available: A, B, C, D, E, F, all"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}=== Batch complete ===${NC}"
