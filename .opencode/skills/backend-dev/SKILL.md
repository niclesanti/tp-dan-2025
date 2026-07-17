---
name: backend-dev
description: Use when implementing, refactoring, or debugging backend features (controllers, services, repositories, DTOs, mappers, entities) in the Spring Boot microservices (user-svc, gestion-svc, reservas-svc) or the shared library dan-common-lib.
---

# Backend Development — Spring Boot Microservices

Use this skill when the task involves writing or modifying Java backend code in any of the 4 Maven modules.

## References

- `AGENTS.md` — project overview, module info, ports, DB types, quick commands

## Module detection

| Module | Path | DB | Persistence | Port |
|--------|------|----|-------------|------|
| user-svc | `services/user-svc` | MySQL | JPA + JDBC | 8081 |
| gestion-svc | `services/gestion-svc` | PostgreSQL | JPA + JDBC + RabbitMQ pub | 8083 |
| reservas-svc | `services/reservas-svc` | MongoDB | Spring Data MongoDB + RabbitMQ sub | 8082 |
| dan-common-lib | `common/dan-common-lib` | — | Shared DTOs/events (Lombok, no Spring) | — |

## Implementation workflow

### 1. Controller layer

```
@Tag(name = "...", description = "...")
@RestController
@RequestMapping("/recurso")
@RequiredArgsConstructor
public class XxxController {

    private final XxxService service;

    @Operation(summary = "...", responses = { @ApiResponse(...) })
    @PostMapping
    public ResponseEntity<XxxDTOResponse> crear(@Valid @RequestBody XxxDTORequest request) {
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }
}
```

### 2. DTOs

- **Request**: `public record` with Jakarta Bean Validation annotations (`@NotBlank`, `@Email`, `@Pattern`, etc.)
- **Response**: `public record` or Lombok `@Data`
- **Shared (dan-common-lib)**: Lombok `@Data @NoArgsConstructor @AllArgsConstructor @Builder` — no Spring dependencies

### 3. Service layer

```
public interface XxxService {
    XxxDTOResponse crear(XxxDTORequest request);
}

@Service
@Slf4j
@RequiredArgsConstructor
public class XxxServiceImpl implements XxxService {

    private final XxxRepository repository;
    private final XxxMapper mapper;

    @Override
    @Transactional
    public XxxDTOResponse crear(XxxDTORequest request) {
        var entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }
}
```

- `@Transactional(readOnly = true)` for queries
- Constructor injection via `@RequiredArgsConstructor`
- Small methods with descriptive names

### 4. Mapper (MapStruct)

```
@Mapper(config = MapstructConfig.class)  // or @Mapper(componentModel = "spring")
public interface XxxMapper {
    XxxDTOResponse toResponse(XxxEntity entity);
    XxxEntity toEntity(XxxDTORequest request);
}
```

### 5. Repository

- **JPA**: `@Repository interface extends JpaRepository<Entity, Integer>`
- **MongoDB**: `interface extends MongoRepository<Entity, String>`
- Custom queries via Spring Data naming or `@Query` only when needed

### 6. Entity

- **JPA**: `@Entity @Table @Data @NoArgsConstructor` + `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
- **MongoDB**: `@Data @Document(collection = "...")` + `@Id private String _id`

## Validation & compilation

```powershell
# Single module (from repo root)
./mvnw -pl services/<modulo> -am clean compile -DskipTests

# If dan-common-lib changed, build it first
./mvnw -pl common/dan-common-lib clean install -DskipTests
```

## Docker deployment & log verification

After implementing changes and passing tests:

```powershell
# Rebuild and start all services
docker compose up -d --build

# Wait for healthy state, then check logs
docker ps
docker logs <container-name> --tail 50
```

- Verify all containers show "Up" / "(healthy)" status.
- Check each service log for errors at startup (port conflicts, DB connectivity, RabbitMQ connection).
- If a container fails: read the log, fix the issue, rebuild, retry.

## Code conventions summary

| Aspect | Convention |
|--------|-----------|
| Injection | Constructor via `@RequiredArgsConstructor` (no `@Autowired` on fields) |
| Validation | Jakarta Bean Validation on request DTO records |
| Mapping | MapStruct (`@Mapper(componentModel = "spring")`) |
| Logging | `@Slf4j` |
| Error handling | Global `@RestControllerAdvice` |
| IDs (JPA) | `@GeneratedValue(IDENTITY)` — Integer |
| IDs (Mongo) | `String _id` + `@Id` |
| Pagination | `Pageable` + `Page<DTO>` + `Page<Entity>` |
