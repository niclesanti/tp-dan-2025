---
applyTo: 'test/**'
---


# Global Testing Standards: Distributed Microservices (Spring Boot 3 & Java 21)

You are a Senior QA Automation Engineer. Follow these strict standards for creating and maintaining tests across the monorepo's three microservices (MySQL, PostgreSQL, and MongoDB).

## 1. Test Data Management (Setup First)
Before writing any test logic, define a clear and reusable data set.
- **Strategy**: Use "Data Builders" or static factory methods in a dedicated `TestDataFactory` class within `src/test/java`.
- **Immutability**: Leverage Java 21 `records` for DTOs and test data containers to ensure data integrity during execution.
- **Isolation**: Each test must have its own unique data set to avoid side effects between test cases.
- **Database Consistency**: Ensure test data reflects the specific constraints of the target DB (e.g., specific ID formats for MongoDB vs. Auto-increment for MySQL).

## 2. Repository Layer (Data Access)
Focus on verifying JPA/Spring Data queries and mapping.
- **SQL Services (MySQL & PostgreSQL)**:
  - Use `@DataJpaTest`.
  - Prefer **H2** or **Testcontainers** (if specific DB features are required).
  - Use `@AutoConfigureTestDatabase` to manage the datasource.
- **NoSQL Service (MongoDB)**:
  - Use `@DataMongoTest`.
  - Use an **Embedded MongoDB** or **Testcontainers** for integration testing.
- **Assertion**: Verify that the entity is correctly persisted and retrieved with all its fields intact.

## 3. Service Layer (Business Logic)
This is the core of the application. Tests here must be fast and isolated.
- **Framework**: JUnit 5 + Mockito.
- **Strategy**: Pure Unit Testing. **Do NOT** use `@SpringBootTest` or load the full Application Context.
- **Setup**: 
  - Annotate the test class with `@ExtendWith(MockitoExtension.class)`.
  - Use `@Mock` for Repository/External Service dependencies.
  - Use `@InjectMocks` for the Service implementation under test.
- **Focus**: Validate complex business rules, exception handling (using `assertThrows`), and interactions with dependencies using `verify()`.

## 4. Controller Layer (API Endpoints)
Verify the web layer without launching a full server.
- **Annotation**: Use `@WebMvcTest(YourController.class)`.
- **Component**: Use `MockMvc` to perform requests and verify responses.
- **Dependency Handling**: Use `@MockBean` to provide mock implementations of the Services called by the Controller.
- **Validation**: 
  - Verify HTTP Status Codes (200, 201, 400, 404, 500).
  - Verify JSON Response structure and content.
  - Check Bean Validation (`@Valid`) triggers for invalid input data.

## 5. Coding Standards & Naming
- **Naming Pattern**: Use the `Given_When_Then` or `MethodName_StateUnderTest_ExpectedBehavior` convention.
- **Language**: All test names, variables, and comments MUST be in English.
- **Assertions**: Use **AssertJ** (`assertThat`) for more readable and fluent assertions.
- **Java 21**: Utilize `var` for local variables in tests to reduce boilerplate code.

## 6. Execution Flow for Agents
When generating a new test:
1. Identify the layer (Controller, Service, or Repository).
2. Look for the corresponding `TestDataFactory`.
3. Generate the test file following the specific annotations above.
4. Execute the test using `./mvnw test -Dtest=ClassName`.
5. If the test fails, analyze the stack trace, fix the implementation or the test, and retry.