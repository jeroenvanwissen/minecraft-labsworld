#!/bin/bash
# Verify task completion and check guardrails
# Usage: ./scripts/verify.sh

set -e

echo "=== Agent Verification ==="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Guardrail limits
MAX_FILES=8
MAX_LINES=500

check_pass() {
    echo -e "${GREEN}✓${NC} $1"
}

check_fail() {
    echo -e "${RED}✗${NC} $1"
}

check_warn() {
    echo -e "${YELLOW}!${NC} $1"
}

# 1. Compile
echo "1. Compiling Kotlin..."
if ./gradlew compileKotlin --quiet; then
    check_pass "Kotlin compilation successful"
else
    check_fail "Kotlin compilation failed"
    exit 1
fi

# 2. Build JAR
echo ""
echo "2. Building shadow JAR..."
if ./gradlew shadowJar --quiet; then
    check_pass "Shadow JAR build successful"
else
    check_fail "Shadow JAR build failed"
    exit 1
fi

# 3. Check diff size
echo ""
echo "3. Checking diff size..."

# Get files changed count
FILES_CHANGED=$(git diff --name-only HEAD~1 2>/dev/null | wc -l | tr -d ' ' || echo "0")

# Get lines changed (insertions + deletions)
DIFF_STATS=$(git diff --stat HEAD~1 2>/dev/null | tail -1 || echo "")
INSERTIONS=$(echo "$DIFF_STATS" | grep -oE '[0-9]+ insertion' | grep -oE '[0-9]+' || echo "0")
DELETIONS=$(echo "$DIFF_STATS" | grep -oE '[0-9]+ deletion' | grep -oE '[0-9]+' || echo "0")
LINES_CHANGED=$((INSERTIONS + DELETIONS))

echo "   Files changed: $FILES_CHANGED (limit: $MAX_FILES)"
echo "   Lines changed: $LINES_CHANGED (limit: $MAX_LINES)"

# Check guardrails
GUARDRAILS_OK=true

if [ "$FILES_CHANGED" -gt "$MAX_FILES" ]; then
    check_warn "GUARDRAIL: More than $MAX_FILES files changed"
    GUARDRAILS_OK=false
else
    check_pass "Files within limit"
fi

if [ "$LINES_CHANGED" -gt "$MAX_LINES" ]; then
    check_warn "GUARDRAIL: More than $MAX_LINES lines changed"
    GUARDRAILS_OK=false
else
    check_pass "Lines within limit"
fi

# 4. Show changed files
echo ""
echo "4. Changed files:"
git diff --name-only HEAD~1 2>/dev/null | head -20 || echo "   (no commits to compare)"

# Summary
echo ""
if [ "$GUARDRAILS_OK" = true ]; then
    echo -e "${GREEN}=== Verification passed ===${NC}"
else
    echo -e "${YELLOW}=== Verification passed with warnings ===${NC}"
    echo "Consider splitting this task into subtasks."
fi
echo ""
