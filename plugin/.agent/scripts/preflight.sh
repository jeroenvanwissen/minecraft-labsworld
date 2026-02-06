#!/bin/bash
# Pre-flight check before running agent tasks
# Usage: ./scripts/preflight.sh

set -e

echo "=== Agent Pre-Flight Check ==="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_pass() {
    echo -e "${GREEN}✓${NC} $1"
}

check_fail() {
    echo -e "${RED}✗${NC} $1"
    exit 1
}

check_warn() {
    echo -e "${YELLOW}!${NC} $1"
}

# 1. Check we're in the right directory
if [ ! -f "build.gradle.kts" ]; then
    check_fail "Not in plugin root directory (missing build.gradle.kts)"
fi
check_pass "In plugin root directory"

# 2. Check git status
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    check_fail "Not a git repository"
fi
check_pass "Git repository detected"

# 3. Check current branch
BRANCH=$(git branch --show-current)
if [ "$BRANCH" = "main" ] || [ "$BRANCH" = "master" ]; then
    check_warn "On $BRANCH branch - create a task branch before making changes"
else
    check_pass "On branch: $BRANCH"
fi

# 4. Check for uncommitted changes
if ! git diff-index --quiet HEAD -- 2>/dev/null; then
    check_warn "Uncommitted changes detected:"
    git status --short
    echo ""
else
    check_pass "Working directory clean"
fi

# 5. Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -ge 21 ]; then
    check_pass "Java version: $JAVA_VERSION"
else
    check_fail "Java 21+ required, found: $JAVA_VERSION"
fi

# 6. Check gradle wrapper
if [ ! -f "./gradlew" ]; then
    check_fail "Gradle wrapper not found"
fi
check_pass "Gradle wrapper present"

# 7. Verify build
echo ""
echo "Running compile check..."
if ./gradlew compileKotlin --quiet 2>/dev/null; then
    check_pass "Kotlin compilation successful"
else
    check_fail "Kotlin compilation failed"
fi

# 8. Check Claude CLI (optional)
if command -v claude &> /dev/null; then
    check_pass "Claude CLI available"
else
    check_warn "Claude CLI not found - install with: npm install -g @anthropic-ai/claude-code"
fi

# 9. Check gh CLI (optional)
if command -v gh &> /dev/null; then
    if gh auth status &> /dev/null; then
        check_pass "GitHub CLI authenticated"
    else
        check_warn "GitHub CLI not authenticated - run: gh auth login"
    fi
else
    check_warn "GitHub CLI not found - install with: brew install gh"
fi

echo ""
echo -e "${GREEN}=== Pre-flight check passed ===${NC}"
echo ""
