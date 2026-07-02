---
name: testing
description: Use when writing or fixing tests with JUnit 5, Mockito, AssertJ, @WebMvcTest, @DataJpaTest, @DataMongoTest, or Testcontainers for the Spring Boot microservices. Covers service-layer unit tests, controller slice tests, and repository data tests.
---

# Testing — JUnit 5 + Mockito

Use this skill when the task involves creating, modifying, or debugging tests.

## References

- `AGENTS.md` — project overview, module structure, JaCoCo thresholds

## Test annotation by layer and module

| Layer | Annotation | Mocks | Module support |
|-------|-----------|-------|----------------|
| Controller | `@WebMvcTest(XxxController.class)` | `@MockBean XxxService` | All 3 services |
| Service | `@ExtendWith(MockitoExtension.class)` | `@Mock` + `@InjectMocks` | All 3 services |
| Repository (JPA) | `@DataJpaTest` + `@ActiveProfiles("test")` | H2 + `TestEntityManager` | user-svc, gestion-svc |
| Repository (Mongo) | `@DataMongoTest` | Embedded Mongo / Testcontainers | reservas-svc |

## Test data factory pattern

Each module has a `TestDataFactory` class at `src/test/java/<base-package>/TestDataFactory.java`:

```java
public final class TestDataFactory {
    private TestDataFactory() {}

    public static Huesped huesped() { ... }
    public static HuespedDTORequest huespedDTORequest() { ... }
    public static HuespedDTOResponse huespedDTOResponse() { ... }
}
```

- Always use existing factory methods when available; extend the factory if needed.
- Each test must use isolated data — no shared mutable state between tests.

## Patterns by layer

### Service layer (fast, isolated, no Spring context)

```java
@ExtendWith(MockitoExtension.class)
class XxxServiceImplTest {

    @Mock private XxxRepository repository;
    @Mock private XxxMapper mapper;
    @InjectMocks private XxxServiceImpl service;

    @Test
    void metodo_Escenario_ResultadoEsperado() {
        var request = TestDataFactory.xxxDTORequest();
        var entity = TestDataFactory.xxxEntity();
        var response = TestDataFactory.xxxDTOResponse();

        when(repository.findById(any())).thenReturn(Optional.of(entity));
        when(mapper.toResponse(any())).thenReturn(response);

        var result = service.metodo(request);

        assertThat(result).isEqualTo(response);
        verify(repository).save(any());
    }
}
```

- `assertThrows` for exception paths
- `verify` for interaction checks
- NO `@SpringBootTest`

### Controller layer (web slice)

```java
@WebMvcTest(XxxController.class)
class XxxControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private XxxService xxxService;

    @Test
    void postEndpoint_DatosValidos_Retorna201() throws Exception {
        var request = TestDataFactory.xxxDTORequest();
        var response = TestDataFactory.xxxDTOResponse();
        when(xxxService.metodo(any())).thenReturn(response);

        mockMvc.perform(post("/recurso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id()));
    }
}
```

### Repository layer (data slice)

```java
@DataJpaTest
@ActiveProfiles("test")
class XxxRepositoryTest {

    @Autowired private XxxRepository repository;
    @Autowired private TestEntityManager entityManager;

    @Test
    void findByXxx_Criterio_RetornaEntidad() {
        var entity = TestDataFactory.xxxEntity();
        entityManager.persist(entity);
        entityManager.flush();

        var result = repository.findByXxx(entity.getXxx());

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo(entity.getNombre());
    }
}
```

- For MongoDB: use `@DataMongoTest` instead of `@DataJpaTest`

## Required coverage (JaCoCo)

JaCoCo enforces **95% branch coverage** on `**/controller/*` and `**/service/*` in every service module.

After running tests, check the report at `services/<modulo>/target/site/jacoco/index.html` if coverage fails.

## Naming & style rules

- AssertJ fluent assertions (`assertThat(...).isEqualTo(...).hasSize(...)`)
- `@DisplayName` descriptive in Spanish or English
- `var` for local variables (Java 21)
- Method/test names: `Metodo_Escenario_ResultadoEsperado` or `Given_When_Then`

## Execution

```powershell
# Single module
./mvnw -pl services/<modulo> test

# All modules
./mvnw test
```

## Checklist

- [ ] Correct annotation per layer (`@ExtendWith`, `@WebMvcTest`, `@DataJpaTest`, `@DataMongoTest`)
- [ ] Uses `TestDataFactory` for test data
- [ ] Covers happy path + edge cases (null, not found, duplicate, invalid input → 400)
- [ ] Uses AssertJ (`assertThat`) not JUnit assertions
- [ ] Uses `verify()` for mock interactions
- [ ] No `@SpringBootTest` for service-layer tests
- [ ] Each test is independent
- [ ] 95% branch coverage on controller/service
