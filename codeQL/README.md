# 🧠 CodeQL Local Analysis Guide

This directory contains everything you need to **run GitHub CodeQL security analysis locally** for our Java/Spring Boot application using Docker.

---

## 📦 Overview

**CodeQL** is GitHub’s static analysis engine that detects potential vulnerabilities, bugs, and security issues in code.

Running CodeQL locally helps you:
- Identify security and quality issues before pushing code.
- Run the same analysis rules as CI.
- View detailed findings directly in VS Code or GitHub.

---

## 🧰 Prerequisites

Before running CodeQL locally, ensure you have:

- **Docker** installed and running  
- (Optional) **VS Code** with these extensions:
  - 🧩 **CodeQL** (by GitHub)
  - 🧩 **SARIF Viewer** (for viewing results)

---

## 🏗️ Step 1 — Build the CodeQL Docker Image

From the project root, build the Docker image located in the `codeQL/` directory:

```bash
docker build -t codeql-java ./codeQL
```

---

## 🚀 Step 2 — Run CodeQL Analysis
Run the following command to analyze the Java codebase from application directory:

```bash
docker run --rm -v $(pwd):/workspace codeql-java bash -c "
  codeql database create /workspace/codeql-db --language=java --source-root=/workspace
  cd /workspace && ./gradlew build -x test
  codeql database analyze /workspace/codeql-db \
    /opt/codeql/codeql/java/ql/src/codeql-suites/java-code-scanning.qls \
    --format=sarif-latest \
    --output=/workspace/codeql-results.sarif
"
```

---

## 📊 Step 3 — View Results
After the analysis completes, you can view the results:
- **In VS Code**: Open the `codeql-results.sarif` file directly to see findings.