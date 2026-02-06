# Agent Container for LabsWorld Plugin Refactoring
#
# This container provides a sandboxed environment with all tools needed
# to run the AI coding agent for executing refactoring tasks.
#
# Uses GitHub Copilot CLI (works with Copilot Individual/Business/Enterprise)

FROM eclipse-temurin:21-jdk-jammy

# Build arguments
ARG NODE_VERSION=20
ARG USERNAME=agent
ARG USER_UID=1000
ARG USER_GID=$USER_UID

# Install system dependencies
RUN apt-get update && apt-get install -y \
    curl \
    git \
    gh \
    jq \
    bc \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install Node.js (for potential tooling)
RUN curl -fsSL https://deb.nodesource.com/setup_${NODE_VERSION}.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
    && mkdir -p /home/$USERNAME/.config \
    && chown -R $USERNAME:$USERNAME /home/$USERNAME

# Create workspace directory
RUN mkdir -p /workspace && chown -R $USERNAME:$USERNAME /workspace

# Switch to non-root user
USER $USERNAME
WORKDIR /workspace

# Install GitHub Copilot CLI extension
RUN gh extension install github/gh-copilot

# Configure git to trust the workspace directory
RUN git config --global --add safe.directory /workspace

# Set default shell
ENV SHELL=/bin/bash

# Health check - verify tools are available
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD java -version && gh --version && gh copilot --help

# Default command - interactive shell
CMD ["/bin/bash"]
