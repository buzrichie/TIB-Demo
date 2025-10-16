# Dockerfile
FROM openjdk:17-slim

# Install basic dependencies
RUN apt-get update && apt-get install -y \
    wget unzip git curl build-essential python3 \
    && rm -rf /var/lib/apt/lists/*

# Install CodeQL CLI
RUN mkdir -p /opt/codeql && \
    wget https://github.com/github/codeql-cli-binaries/releases/download/v2.18.2/codeql-linux64.zip && \
    unzip codeql-linux64.zip -d /opt/codeql && \
    rm codeql-linux64.zip

ENV PATH="/opt/codeql/codeql:${PATH}"

# Set up workspace
WORKDIR /workspace

ENTRYPOINT ["/bin/bash"]
