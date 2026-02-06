#!/bin/bash
# Prepare branch for a task and open VS Code
# Usage: ./scripts/run-task.sh <TASK_ID> [--no-open]
#
# This script:
# 1. Runs pre-flight checks
# 2. Creates/switches to the task branch
# 3. Opens VS Code with the task file
# 4. After you complete the task with Copilot, run verify.sh
#
# Examples:
#   ./scripts/run-task.sh A1
#   ./scripts/run-task.sh A1 --no-open

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

if [ -z "$1" ]; then
    echo "Usage: $0 <TASK_ID> [--no-open]"
    echo ""
    echo "Examples:"
    echo "  $0 A1           # Prepare for task A1 and open VS Code"
    echo "  $0 B2 --no-open # Prepare for task B2, don't open VS Code"
    exit 1
fi

TASK_ID="$1"
NO_OPEN="${2:-}"
BRANCH_NAME="refactor/$(echo "$TASK_ID" | tr '[:upper:]' '[:lower:]')"

echo -e "${CYAN}=== Preparing Task $TASK_ID ===${NC}"
echo ""

# Change to workspace root
cd "$(dirname "$0")/../.."

# Pre-flight check
echo "Running pre-flight check..."
./.agent/scripts/preflight.sh

# Ensure we're on main and up to date
echo ""
echo "Preparing branch..."
git checkout main
git pull origin main 2>/dev/null || true

# Create task branch
if git show-ref --verify --quiet "refs/heads/$BRANCH_NAME"; then
    echo -e "${YELLOW}Branch $BRANCH_NAME already exists. Checking out...${NC}"
    git checkout "$BRANCH_NAME"
else
    git checkout -b "$BRANCH_NAME"
    echo -e "${GREEN}Created branch: $BRANCH_NAME${NC}"
fi

echo ""
echo -e "${GREEN}=== Ready for Task $TASK_ID ===${NC}"
echo ""
echo "Next steps:"
echo "  1. Open VS Code with Copilot Chat (Cmd+Shift+I)"
echo "  2. Say: 'Execute task $TASK_ID from .agent/TASKS.md'"
echo "  3. Review Copilot's changes"
echo "  4. Run: ./.agent/scripts/verify.sh"
echo "  5. Commit: git add -A && git commit -m 'refactor: task $TASK_ID'"
echo "  6. Create PR: gh pr create --fill"
echo ""

# Open VS Code unless --no-open specified
if [ "$NO_OPEN" != "--no-open" ]; then
    echo "Opening VS Code..."
    code . .agent/TASKS.md
fi
