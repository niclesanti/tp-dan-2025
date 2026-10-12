# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

See `AGENTS.md` at the repo root — it is the canonical, actively-maintained reference for this project (modules, commands, architecture, frontend conventions, gotchas, testing, profiles, ports). Read it first; the notes below only add single-test invocations and module-build order that AGENTS.md doesn't spell out.

## Running a single test

```powershell
# Single test class (any service module)
cd services/user-svc && ./mvnw test -Dtest=ClassNameTest

# Single test method
cd services/user-svc && ./mvnw test -Dtest=ClassNameTest#methodName

# Build dan-common-lib first if a service module fails to resolve it
cd common/dan-common-lib && ./mvnw clean install -DskipTests
```

## Build order for local (non-Docker) work

`dan-common-lib` must be installed to the local Maven repo before `gestion-svc` or `reservas-svc` will compile, since both declare it as a dependency (see AGENTS.md's `dan-common-lib` row). There is no such ordering requirement for Docker builds — each service's Dockerfile builds the reactor with `-pl <module> -am`, which builds `dan-common-lib` automatically.
