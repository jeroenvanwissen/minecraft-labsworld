# Testing Conventions

## Framework

- **Test runner:** JUnit 5 (Jupiter) 5.11.4
- **Assertion library:** `org.junit.jupiter.api.Assertions.*`
- **Mocking:** MockK 1.13.14

## File Organization

- Test location: `plugin/src/test/kotlin/` mirroring source package structure
- Test file naming: `{SourceClass}Test.kt`
- Test fixtures: `plugin/src/test/resources/fixtures/twitch/*.yml`

## Test Structure

```kotlin
class FeatureTest {

    private lateinit var plugin: JavaPlugin
    private lateinit var systemUnderTest: MyService

    @BeforeEach
    fun setup() {
        plugin = mockk(relaxed = true)
        // wire dependencies
        systemUnderTest = MyService(plugin)
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    // ==================== Feature Group ====================

    @Nested
    @DisplayName("Feature Group")
    inner class FeatureGroupTests {

        @Test
        @DisplayName("should do something when condition")
        fun descriptiveName() {
            // arrange
            // act
            // assert
        }
    }
}
```

## Naming

- Test suites: `@DisplayName("Feature Group")` on `@Nested inner class`
- Test cases: `@DisplayName("should verb when condition")` on `@Test fun descriptiveName()`
- Function names: camelCase describing the scenario (`validInteger`, `unauthorizedCommand`)
- Section comments: `// ==================== Section Name ====================`

## Patterns

### Setup and Teardown

- `@BeforeEach fun setup()` — initialize mocks and test fixtures
- `@AfterEach fun cleanup()` — call `unmockkAll()` or `clearAllMocks()`
- Use `createTempDir()` for file I/O tests; `deleteRecursively()` in teardown
- Create factory methods for common mock objects (`createMockEvent()`, `createBroadcasterEvent()`)

### Mocking

- Use relaxed mocks: `mockk<T>(relaxed = true)` to avoid unmocked method issues
- Mock Bukkit scheduler to execute synchronously:
  ```kotlin
  every { scheduler.runTask(plugin, any<Runnable>()) } answers {
      secondArg<Runnable>().run()
      mockk(relaxed = true)
  }
  ```
- Use `mockkStatic(ClassName::class)` for static method mocking (e.g., `EntityType`)
- Use reflection to inject fixture data into private fields when needed
- Do NOT mock: business logic, pure functions, data classes

### Assertions

- Use JUnit 5 assertions: `assertEquals`, `assertTrue`, `assertFalse`, `assertNull`, `assertNotNull`
- Use `assertDoesNotThrow { }` for verifying no exceptions
- Use `match<T> { condition }` for complex MockK verification
- One logical assertion per test (multiple `assertEquals` on related fields is fine)

### Parameterized Tests

- Use `@ParameterizedTest` with `@CsvSource` for matrix-style testing
- Name pattern: `@ParameterizedTest(name = "{0} for {1} should be {2}")`

## Coverage

- **Minimum coverage:** 75% line coverage (enforced by JaCoCo)
- **Coverage command:** `plugin/gradlew jacocoTestReport`
- **Verification command:** `plugin/gradlew jacocoTestCoverageVerification`
- **Reports:** `plugin/build/reports/jacoco/test/html/`
- **Configuration:** `jacocoMinCoverage` property in `plugin/gradle.properties`

## Known Limitations

- Bukkit Registry System cannot be fully initialized in unit tests
- Entity mocking limited — static initializers in Bukkit entity classes access server registry
- Use real temp directories for file I/O tests instead of mocking `YamlConfiguration`

## Do / Don't

| Do                                              | Don't                                        |
| ----------------------------------------------- | -------------------------------------------- |
| `mockk<T>(relaxed = true)`                      | Non-relaxed mocks without full stubbing      |
| `@DisplayName("should verb when condition")`    | Rely on function name alone for test intent  |
| `@Nested inner class` for grouping              | Flat test class with 50+ methods             |
| `unmockkAll()` in `@AfterEach`                  | Leave mocks leaking between tests            |
| Factory methods for mock objects                | Duplicate mock setup across tests            |
| Real temp directories for file tests            | Mock filesystem operations                   |
