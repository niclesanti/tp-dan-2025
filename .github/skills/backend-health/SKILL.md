---
name: backend-health
description: Recursive workflow to compile, deploy, and test the backend, automatically fixing errors until everything passes.
---

# Backend Self-Healing Instructions

When this skill is activated, run the following sequential execution loop for this monorepo.

## Scope Detection (Mandatory)
1. Detect impacted modules first:
   - `services/user-svc` (MySQL)
   - `services/gestion-svc` (PostgreSQL)
   - `services/reservas-svc` (MongoDB)
   - `common/dan-common-lib` (shared library), if touched
2. If the request does not specify modules, infer them from changed files.
3. Prefer targeted validation for impacted modules before full-repo validation.

## Phase 1: Clean Compilation
1. Run clean compilation for each impacted module.
2. Preferred execution from each module directory:
   - `./mvnw clean compile`
3. If module dependencies require it, run from repo root with Maven reactor:
   - `./mvnw -pl <module-path> -am clean compile`
4. **Fix Loop:** If compilation fails (exit code != 0):
   - Read the terminal error logs.
   - Apply the required fixes in Java code following `backend-expert.instructions.md`.
   - Retry Phase 1 until it succeeds.

## Phase 2: Infrastructure Deployment
1. Execute this phase when backend runtime, infra, Docker, messaging, or DB-init changes are involved.
2. Run `docker compose up -d --build` from repository root.
3. Run `docker ps` to verify container status.
4. **Fix Loop:** If any service (`db`, `backend`) is not "Up":
   - Run `docker logs [container_name]`.
   - Analyze whether the issue is configuration, port conflict, or connectivity.
   - Apply the required fixes.
   - Retry step 1.

## Phase 3: Logic Validation
1. Run tests for each impacted module:
   - `./mvnw test`
2. If cross-module impact exists, run a broader validation from repo root:
   - `./mvnw test`
3. **Fix Loop:** If there are failing tests:
   - Read the JUnit report in the console.
   - Fix the affected service logic following `backend-expert.instructions.md`, or fix the test if the test is incorrect.
   - Retry Phase 3 until 100% of tests pass.

## Final Objective
Do not mark the task as complete until all phases succeed fully. Do not ask for permission to fix obvious errors found in logs.