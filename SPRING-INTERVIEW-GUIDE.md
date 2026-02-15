# Java Spring Developer Interview Questions & Answers

**Repository:** [calvinlee999/java-spring-kafka](https://github.com/calvinlee999/java-spring-kafka)

**Purpose:** Comprehensive interview preparation guide covering Java Spring, Spring Boot, REST APIs, Data/Testing, and Security.

---

## Table of Contents
1. [Java Spring and Spring Boot](#java-spring-and-spring-boot)
2. [Spring REST API](#spring-rest-api)
3. [Data Testing and Persistence](#data-testing-and-persistence)
4. [Security](#security)
5. [Self-Evaluation Reviews](#self-evaluation-reviews)

---

## Java Spring and Spring Boot

### Q1: What are the key differences between the traditional Spring Framework and Spring Boot?

**Answer:**

The key differences can be categorized into several areas:

**Configuration:**
- **Traditional Spring:** Requires extensive XML or Java-based configuration. You must manually configure components like DispatcherServlet, ViewResolver, DataSource, etc.
- **Spring Boot:** Uses auto-configuration based on classpath dependencies. Configuration is minimal with sensible defaults.

**Dependency Management:**
- **Traditional Spring:** You manually manage all dependencies and their compatible versions in your build file.
- **Spring Boot:** Provides "starter" POMs that bundle related dependencies with compatible versions (e.g., `spring-boot-starter-web`).

**Embedded Server:**
- **Traditional Spring:** Requires external application server (Tomcat, JBoss, WebLogic) for deployment. Application is packaged as WAR file.
- **Spring Boot:** Includes embedded server (Tomcat, Jetty, or Undertow). Application can run standalone as executable JAR with `java -jar`.

**Production-Ready Features:**
- **Traditional Spring:** Requires manual setup for metrics, health checks, monitoring.
- **Spring Boot:** Comes with Spring Boot Actuator providing production-ready features out-of-the-box.

**Development Experience:**
- **Traditional Spring:** Slower development cycle, more boilerplate code.
- **Spring Boot:** Faster development with Spring Boot DevTools (auto-restart), CLI, and Spring Initializr.

**Example Comparison:**

Traditional Spring web.xml:
```xml
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/spring-config.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
```

Spring Boot equivalent:
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

### Q2: Explain the purpose of the @SpringBootApplication annotation and what it enables internally.

**Answer:**

`@SpringBootApplication` is a convenience annotation that combines three key annotations:

**1. @Configuration** 
- Marks the class as a source of bean definitions
- Allows you to define @Bean methods that create Spring-managed objects

**2. @EnableAutoConfiguration**
- Enables Spring Boot's auto-configuration mechanism
- Automatically configures beans based on classpath dependencies
- For example, if `spring-boot-starter-web` is on classpath, it auto-configures:
  - Embedded Tomcat server
  - DispatcherServlet
  - Default error pages
  - HTTP message converters (JSON with Jackson)

**3. @ComponentScan**
- Scans the package of the annotated class and all sub-packages
- Discovers and registers Spring components (@Component, @Service, @Repository, @Controller)
- Default scan starts from the package where @SpringBootApplication class resides

**Full Equivalent:**
```java
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.example")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Internal Process:**
1. Spring Boot scans for `META-INF/spring.factories` files
2. Loads auto-configuration classes (e.g., `DataSourceAutoConfiguration`)
3. Applies conditional logic (@ConditionalOnClass, @ConditionalOnMissingBean)
4. Registers beans only if conditions are met
5. Allows user-defined beans to override auto-configured ones

---

### Q3: What are "starter dependencies" in Spring Boot, and why are they useful?

**Answer:**

Starter dependencies are curated sets of dependencies bundled together for specific functionality. They solve the "dependency hell" problem.

**Key Benefits:**

**1. Simplified Dependency Management**
- Instead of adding 10+ individual dependencies, add one starter
- All dependencies are pre-tested for version compatibility

**2. Consistent Versioning**
- Spring Boot manages versions through dependency management
- No need to specify version numbers for most dependencies
- Prevents version conflicts

**3. Logical Grouping**
- Dependencies are grouped by functionality/use-case
- Makes it obvious what capabilities you're adding

**Common Starters:**

```xml
<!-- Web Applications -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<!-- Includes: Spring MVC, Tomcat, Jackson, Validation, etc. -->

<!-- Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<!-- Includes: Hibernate, Spring Data JPA, JDBC, Transaction API -->

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<!-- Includes: Spring Security Core, Config, Web -->

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<!-- Includes: JUnit, Mockito, AssertJ, Hamcrest, Spring Test -->
```

**How It Works:**
Each starter is essentially a POM file that declares transitive dependencies. When you add a starter, Maven/Gradle automatically pulls all related dependencies.

---

### Q4: How does Spring Boot's auto-configuration work, and how can you customize or disable specific auto-configurations?

**Answer:**

**How Auto-Configuration Works:**

**Step 1: Classpath Scanning**
- Spring Boot scans for `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Lists all auto-configuration classes available

**Step 2: Conditional Evaluation**
- Each auto-configuration class has conditions (@Conditional annotations)
- Common conditions:
  - `@ConditionalOnClass` - Only if specific class is on classpath
  - `@ConditionalOnMissingBean` - Only if bean doesn't already exist
  - `@ConditionalOnProperty` - Only if specific property is set

**Step 3: Bean Registration**
- If all conditions are met, Spring registers the beans
- User-defined beans always take precedence

**Example Auto-Configuration:**
```java
@Configuration
@ConditionalOnClass({DataSource.class, EmbeddedDatabaseType.class})
@ConditionalOnMissingBean(DataSource.class)
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
```

**Customization Methods:**

**1. Override by Creating Your Own Bean**
```java
@Configuration
public class MyConfig {
    @Bean
    public DataSource dataSource() {
        // Your custom DataSource
        // This will disable DataSourceAutoConfiguration
    }
}
```

**2. Use Application Properties**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.hikari.maximum-pool-size=20
```

**3. Exclude Specific Auto-Configurations**
```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
public class Application {
    // ...
}
```

Or via properties:
```properties
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

**4. Debug Auto-Configuration**
```properties
# See which auto-configurations are applied
debug=true
# Or run with --debug flag
```

This generates an auto-configuration report showing:
- Positive matches (what was configured)
- Negative matches (what was not configured and why)
- Exclusions
- Unconditional classes

---

### Q5: What is the Inversion of Control (IoC) container, and how does dependency injection (DI) work in Spring?

**Answer:**

**Inversion of Control (IoC):**

IoC is a design principle where the control of object creation and lifecycle is transferred from the application code to the framework.

**Traditional Approach (You Control):**
```java
public class UserService {
    private UserRepository userRepository = new UserRepository(); // You create it
    
    public User getUser(Long id) {
        return userRepository.findById(id);
    }
}
```

**Problems:**
- Tight coupling
- Hard to test (can't mock UserRepository)
- Hard to change implementation
- You manage lifecycle

**IoC Approach (Framework Controls):**
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    @Autowired // Framework injects it
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User getUser(Long id) {
        return userRepository.findById(id);
    }
}
```

**Benefits:**
- Loose coupling
- Easy to test (inject mocks)
- Easy to swap implementations
- Framework manages lifecycle

**The IoC Container:**

Spring's IoC container is responsible for:
1. **Creating objects (beans)**
2. **Managing their lifecycle** (initialization, destruction)
3. **Injecting dependencies**
4. **Wiring beans together**

Two main container types:
- `BeanFactory` - Basic container
- `ApplicationContext` - Advanced container (recommended)

**Dependency Injection (DI) Types:**

**1. Constructor Injection (Recommended)**
```java
@Service
public class OrderService {
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    
    // Spring automatically injects dependencies
    // @Autowired optional on single constructor
    public OrderService(PaymentService paymentService, 
                       InventoryService inventoryService) {
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
    }
}
```

**Advantages:**
- Immutable (final fields)
- All dependencies required (fail-fast)
- Easy to test
- No reflection needed for testing

**2. Setter Injection**
```java
@Service
public class EmailService {
    private TemplateEngine templateEngine;
    
    @Autowired
    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
}
```

**Use for:**
- Optional dependencies
- Dependencies that may change

**3. Field Injection (Not Recommended)**
```java
@Service
public class ReportService {
    @Autowired
    private DataService dataService; // Avoid this
}
```

**Problems:**
- Cannot create immutable objects
- Hard to test (requires Spring context)
- Hides dependencies
- Cannot inject into final fields

**Bean Scopes:**
- `@Scope("singleton")` - One instance per container (default)
- `@Scope("prototype")` - New instance each time
- `@Scope("request")` - One instance per HTTP request
- `@Scope("session")` - One instance per HTTP session

---

### Q6: Explain the difference between @Component, @Service, and @Repository annotations.

**Answer:**

All three annotations are **specializations of @Component** and mark a class as a Spring-managed bean. However, they serve different semantic purposes and have distinct capabilities.

**@Component - Generic Stereotype**
```java
@Component
public class EmailValidator {
    public boolean isValid(String email) {
        // Validation logic
    }
}
```

**Purpose:**
- Generic Spring-managed component
- Use when the class doesn't fit into other categories
- Base annotation for other stereotypes

**@Service - Business Logic Layer**
```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public User createUser(User user) {
        // Business logic
        validateUser(user);
        return userRepository.save(user);
    }
    
    private void validateUser(User user) {
        // Validation logic
    }
}
```

**Purpose:**
- Marks service layer classes
- Contains business logic
- Often transactional
- Orchestrates multiple repositories

**Characteristics:**
- Clear semantic meaning
- Can contain complex business rules
- May call multiple repositories
- Good place for @Transactional

**@Repository - Data Access Layer**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findActiveUsers();
}
```

**Purpose:**
- Marks data access layer classes
- Interacts with database
- Abstracts persistence logic

**Special Features:**

**1. Exception Translation**
- Automatically converts database exceptions to Spring's DataAccessException hierarchy
- Makes exception handling database-agnostic

```java
@Repository
public class JdbcUserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public User findById(Long id) {
        // SQLException is automatically translated to DataAccessException
        return jdbcTemplate.queryForObject(
            "SELECT * FROM users WHERE id = ?",
            new UserRowMapper(),
            id
        );
    }
}
```

**2. Persistence Exception Handling**
- JPA exceptions (e.g., `EntityNotFoundException`) are caught and translated
- Hibernate exceptions are translated to Spring exceptions

**Layered Architecture:**

```
┌─────────────────────┐
│   @Controller       │  ← Presentation Layer
│   (REST/Web)        │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│   @Service          │  ← Business Logic Layer
│   (Business Logic)  │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│   @Repository       │  ← Data Access Layer
│   (Database)        │
└─────────────────────┘
```

**Comparison Table:**

| Aspect | @Component | @Service | @Repository |
|--------|-----------|----------|-------------|
| Layer | Any | Business Logic | Data Access |
| Purpose | Generic component | Business operations | Database operations |
| Exception Translation | No | No | Yes (DataAccessException) |
| Typical Usage | Utilities, Helpers | Business services | DAOs, JPA Repositories |
| Transactions | Can have | Usually has | Usually has |
| Semantic Meaning | Generic | Clear (service) | Clear (data access) |

**Best Practice:**
Use the most specific annotation for better code clarity and to utilize framework features like exception translation.

---

### Q7: When would you use @ConfigurationProperties versus @Value for externalizing configuration?

**Answer:**

Both annotations externalize configuration, but they serve different use cases.

**@Value - Simple Properties**

**Use When:**
- You need a single property value
- Simple type injection
- Property is used in one place
- No validation needed

**Example:**
```java
@Service
public class EmailService {
    
    @Value("${email.from}")
    private String fromAddress;
    
    @Value("${email.smtp.host}")
    private String smtpHost;
    
    @Value("${email.smtp.port}")
    private int smtpPort;
    
    @Value("${feature.email.enabled:true}") // Default value
    private boolean emailEnabled;
}
```

**Properties File:**
```properties
email.from=noreply@example.com
email.smtp.host=smtp.gmail.com
email.smtp.port=587
feature.email.enabled=true
```

**Limitations:**
- Scattered across multiple classes
- No type safety
- No validation
- Hard to test
- No IDE autocomplete for properties

**@ConfigurationProperties - Complex Configuration**

**Use When:**
- Multiple related properties
- Need validation
- Type-safe configuration
- Hierarchical/nested properties
- Reusable configuration across classes

**Example:**
```java
@ConfigurationProperties(prefix = "email")
@Validated
public class EmailProperties {
    
    @NotBlank
    private String from;
    
    @Valid
    private Smtp smtp = new Smtp();
    
    private Feature feature = new Feature();
    
    // Getters and setters
    
    public static class Smtp {
        @NotBlank
        private String host;
        
        @Min(1)
        @Max(65535)
        private int port = 587;
        
        private boolean auth = true;
        
        private boolean starttls = true;
        
        // Getters and setters
    }
    
    public static class Feature {
        private boolean enabled = true;
        
        // Getters and setters
    }
}
```

**Enable Configuration Properties:**
```java
@Configuration
@EnableConfigurationProperties(EmailProperties.class)
public class EmailConfig {
    // Configuration beans
}
```

**Or use @ConfigurationPropertiesScan:**
```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {
    // ...
}
```

**Usage in Service:**
```java
@Service
public class EmailService {
    
    private final EmailProperties emailProperties;
    
    public EmailService(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
    }
    
    public void sendEmail(String to, String subject, String body) {
        if (!emailProperties.getFeature().isEnabled()) {
            return; // Email feature disabled
        }
        
        // Use emailProperties.getFrom(), emailProperties.getSmtp().getHost(), etc.
    }
}
```

**Properties File (YAML preferred for hierarchical):**
```yaml
email:
  from: noreply@example.com
  smtp:
    host: smtp.gmail.com
    port: 587
    auth: true
    starttls: true
  feature:
    enabled: true
```

**Advantages of @ConfigurationProperties:**

1. **Type Safety**
   - Compile-time checking
   - IDE autocomplete

2. **Validation**
   - Use Bean Validation annotations (@NotNull, @Min, @Max, @Pattern)
   - Fail fast on invalid configuration

3. **Structure**
   - Nested properties
   - Clear organization
   - Single source of truth

4. **Testability**
   - Easy to create test configurations
   - Can use POJOs in tests

5. **Documentation**
   - Self-documenting configuration
   - Can generate metadata for IDE support

6. **Relaxed Binding**
   - Supports multiple formats: camelCase, kebab-case, snake_case, UPPER_CASE
   - `email.smtp.host` = `email.smtp-host` = `EMAIL_SMTP_HOST`

**Comparison Table:**

| Feature | @Value | @ConfigurationProperties |
|---------|--------|--------------------------|
| Single property | ✅ Good | ⚠️ Overkill |
| Multiple related properties | ❌ Scattered | ✅ Excellent |
| Type safety | ❌ Limited | ✅ Full |
| Validation | ❌ No | ✅ Yes |
| Nested properties | ❌ Difficult | ✅ Easy |
| Testability | ⚠️ Harder | ✅ Easy |
| Relaxed binding | ✅ Yes | ✅ Yes |
| Default values | ✅ Inline | ✅ In class |
| SpEL support | ✅ Yes | ❌ No |

**Best Practice:**
- Use **@Value** for simple, standalone properties
- Use **@ConfigurationProperties** for grouped, related configuration with 3+ properties
- Prefer **@ConfigurationProperties** for production applications (better maintainability)

---

### Q8: What are Spring Profiles, and how do you use them to manage environment-specific configurations (e.g., dev, test, prod)?

**Answer:**

Spring Profiles allow you to segregate parts of your application configuration and make them available only in certain environments.

**Use Cases:**
- Different databases per environment
- Different API endpoints
- Different logging levels
- Feature toggles
- Environment-specific beans

**Defining Profiles:**

**1. Profile-Specific Properties Files**

```
application.properties (default for all profiles)
application-dev.properties (development)
application-test.properties (testing)
application-prod.properties (production)
```

**application.properties (common):**
```properties
# Common configuration
app.name=MyApplication
app.version=1.0.0
```

**application-dev.properties:**
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=

logging.level.com.example=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# Dev-specific features
feature.email.enabled=false
feature.cache.enabled=false
```

**application-test.properties:**
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa  
spring.datasource.password=

logging.level.com.example=INFO

feature.email.enabled=false
feature.cache.enabled=true
```

**application-prod.properties:**
```properties
spring.datasource.url=jdbc:mysql://prod-db.example.com:3306/mydb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=50

logging.level.com.example=WARN
logging.level.org.hibernate.SQL=WARN

feature.email.enabled=true
feature.cache.enabled=true
```

**2. Profile-Specific Beans**

```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }
    
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        dataSource.setMaximumPoolSize(50);
        return dataSource;
    }
}
```

**3. Profile on Component Classes**

```java
@Service
@Profile("dev")
public class MockEmailService implements EmailService {
    public void sendEmail(String to, String subject, String body) {
        System.out.println("MOCK: Sending email to " + to);
    }
}

@Service
@Profile("prod")
public class SmtpEmailService implements EmailService {
    public void sendEmail(String to, String subject, String body) {
        // Real SMTP implementation
    }
}
```

**Activating Profiles:**

**Method 1: application.properties**
```properties
spring.profiles.active=dev
```

**Method 2: Environment Variable**
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar myapp.jar
```

**Method 3: Command Line Argument**
```bash
java -jar myapp.jar --spring.profiles.active=prod
```

**Method 4: JVM System Property**
```bash
java -Dspring.profiles.active=prod -jar myapp.jar
```

**Method 5: Programmatically**
```java
public static void main(String[] args) {
    SpringApplication app = new SpringApplication(Application.class);
    app.setAdditionalProfiles("prod");
    app.run(args);
}
```

**Method 6: In Tests**
```java
@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {
    // Tests run with 'test' profile
}
```

**Multiple Profiles:**

You can activate multiple profiles simultaneously:

```bash
java -jar myapp.jar --spring.profiles.active=prod,monitoring,eu-region
```

**Profile precedence (last wins):**
```properties
spring.profiles.active=dev,override
# 'override' profile properties will override 'dev' profile
```

**Profile Groups (Spring Boot 2.4+):**

Group related profiles together:

```properties
spring.profiles.group.production=prod,monitoring,alerts
spring.profiles.group.development=dev,debug
```

Activate group:
```bash
java -jar myapp.jar --spring.profiles.active=production
# Activates: prod, monitoring, alerts
```

**Conditional on Profile:**

```java
@Configuration
public class CacheConfig {
    
    @Bean
    @Profile("!prod") // NOT prod (dev or test)
    public CacheManager noCacheManager() {
        return new NoOpCacheManager();
    }
    
    @Bean
    @Profile("prod")
    public CacheManager redisCacheManager() {
        return RedisCacheManager.create(redisConnectionFactory);
    }
}
```

**Profile Expressions:**

```java
@Profile("prod & eu-region") // AND
@Profile("dev | test") // OR  
@Profile("!prod") // NOT
```

**Best Practices:**

1. **Keep common configuration in application.properties**
2. **Override only what's different in profile-specific files**
3. **Use environment variables for secrets in production**
4. **Never commit production credentials to version control**
5. **Use @Profile for beans, profile-specific files for properties**
6. **Test with appropriate profiles**
7. **Document which profiles exist and their purpose**

**Example Project Structure:**
```
src/main/resources/
├── application.properties          # Common config
├── application-dev.properties      # Development overrides
├── application-test.properties     # Test overrides
├── application-prod.properties     # Production overrides
└── application-local.properties    # Local developer overrides (git-ignored)
```

---

## Spring REST API

### Q9: What does "stateless" mean in the context of a REST API, and how is it implemented in Spring Boot?

**Answer:**

**Stateless Concept:**

Stateless means each HTTP request is **completely independent** and contains all the information needed to process it. The server does not store any client context between requests.

**Key Principles:**

1. **No Session Storage on Server**
   - Server doesn't remember previous interactions
   - No memory of client state between requests

2. **Self-Contained Requests**
   - Each request contains all necessary information
   - Authentication/authorization in every request
   - No dependency on previous requests

3. **Scalability**
   - Any server instance can handle any request
   - Easy horizontal scaling
   - No session affinity needed

**Stateful vs Stateless:**

**Stateful (Traditional Web Apps):**
```
Client                          Server
  |-----Login (user/pass)------>|
  |<----Session ID (cookie)-----|  [Store: session123 = {user: "john"}]
  |                              |
  |-----Get Profile------------>|  
  |  (cookie: session123)        |  [Lookup session, get user]
  |<----User Data---------------|
  |                              |
  |-----Update Profile--------->|
  |  (cookie: session123)        |  [Lookup session, get user, update]
  |<----Success-----------------|
```

**Problems with Stateful:**
- Server must store sessions (memory/database)
- Load balancer needs sticky sessions
- Doesn't scale horizontally easily
- Session cleanup required

**Stateless (REST API):**
```
Client                          Server
  |-----Login (user/pass)------>|
  |<----JWT Token---------------|  [No storage, just return token]
  |                              |
  |-----Get Profile------------>|
  |  Authorization: Bearer JWT   |  [Decode JWT, verify, get user from token]
  |<----User Data---------------|
  |                              |
  |-----Update Profile--------->|
  |  Authorization: Bearer JWT   |  [Decode JWT, verify, update]
  |<----Success-----------------|
```

**Benefits of Stateless:**
- Any server can handle any request
- Easy horizontal scaling
- No session storage/management
- Simpler infrastructure

**Implementation in Spring Boot:**

**1. Stateless Authentication with JWT**

```java
// Generate JWT on login
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        // Generate JWT token (contains user info)
        String token = tokenProvider.generateToken(authentication);
        
        // Return token to client (no server-side session)
        return ResponseEntity.ok(new JwtAuthResponse(token));
    }
}
```

**2. Validate JWT on Every Request**

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Extract JWT from Authorization header
        String jwt = getJwtFromRequest(request);
        
        if (jwt != null && tokenProvider.validateToken(jwt)) {
            // Decode JWT and extract user info (from token, not database)
            String username = tokenProvider.getUsernameFromJWT(jwt);
            
            // Set authentication in SecurityContext (request-scoped)
            UserDetails userDetails = loadUserDetails(username);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
        // After request, SecurityContext is cleared (stateless)
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

**3. Disable Session Creation**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No sessions!
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                           UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**4. Stateless Controller Example**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile() {
        // Get current user from SecurityContext
        // (populated from JWT token in this request)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Fetch data (no session lookup)
        UserProfile profile = userService.getUserProfile(username);
        
        return ResponseEntity.ok(profile);
        // SecurityContext cleared after response (no state retained)
    }
}
```

**Key Implementation Points:**

1. **Authentication in Every Request**
   - Client sends credentials/token with each request
   - Server validates on each request

2. **No Session Storage**
   - `SessionCreationPolicy.STATELESS`
   - No JSESSIONID cookies
   - No server-side session objects

3. **Token-Based Authentication**
   - JWT contains user information
   - Server verifies token signature
   - No database lookup for session (optionally may verify user still exists)

4. **SecurityContext is Request-Scoped**
   - Populated at start of request
   - Cleared at end of request
   - Never persists between requests

**REST API Request Example:**

```bash
# Login - get token
curl -X POST http://api.example.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"secret"}'

# Response: {"token":"eyJhbGc..."}

# Get profile - send token with every request
curl http://api.example.com/api/users/profile \
  -H "Authorization: Bearer eyJhbGc..."

# Update profile - send token again
curl -X PUT http://api.example.com/api/users/profile \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{"email":"new@example.com"}'
```

Each request is independent and contains all necessary authentication information.

---

### Q10: Explain the differences between @Controller and @RestController.

**Answer:**

Both annotations are used to create web controllers, but they serve different purposes and have different default behavior.

**@Controller - Traditional MVC**

**Purpose:**
- Creates traditional web applications
- Returns **views** (HTML pages)
- Works with template engines (Thymeleaf, JSP, Freemarker)

**Example:**
```java
@Controller
@RequestMapping("/web")
public class WebController {
    
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("message", "Welcome!");
        return "home"; // Returns view name (home.html)
    }
    
    @GetMapping("/users/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        User user = userService.getUser(id);
        model.addAttribute("user", user);
        return "user-details"; // Returns view name
    }
    
    // To return JSON data, must use @ResponseBody
    @GetMapping("/api/users/{id}")
    @ResponseBody // Explicitly needed!
    public User getUserAsJson(@PathVariable Long id) {
        return userService.getUser(id); // Serialized to JSON
    }
}
```

**Behavior:**
- Return value is interpreted as **view name**
- Spring looks for view template (e.g., `home.html`)
- ViewResolver resolves view name to actual template
- Response is rendered HTML

**@RestController - REST APIs**

**Purpose:**
- Creates RESTful web services
- Returns **data** (JSON/XML)  
- Combination of @Controller + @ResponseBody on every method

**Equivalent Definition:**
```java
@RestController = @Controller + @ResponseBody
```

**Example:**
```java
@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUser(id); 
        // Automatically serialized to JSON (no @ResponseBody needed)
    }
    
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
        // Returns JSON array
    }
    
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.save(user);
        // Request body converted from JSON to User object
        // Response converted from User object to JSON
    }
    
    @GetMapping("/users/{id}/status")
    public ResponseEntity<UserStatus> getUserStatus(@PathVariable Long id) {
        UserStatus status = userService.getUserStatus(id);
        return ResponseEntity.ok(status);
        // Returns JSON with HTTP 200
    }
}
```

**Behavior:**
- Return value is **automatically serialized to JSON/XML**
- No view resolution
- Uses HttpMessageConverters (Jackson for JSON by default)
- @ResponseBody is implicit on all methods

**Comparison Table:**

| Aspect | @Controller | @RestController |
|--------|-------------|-----------------|
| Primary Use | Web pages (MVC) | REST APIs |
| Return Type | View name (String) | Data (Object) |
| Response Format | HTML | JSON/XML |
| @ResponseBody | Required for JSON | Implicit (automatic) |
| Template Engine | Yes (Thymeleaf, JSP) | No |
| Model Attribute | Yes | No (not needed) |
| ViewResolver | Yes | No |
| HTTP Message Converter | Only with @ResponseBody | Always |

**Internal Composition:**

```java
// @Controller
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Controller {
}

// @RestController
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller  // Extends @Controller
@ResponseBody  // Adds @ResponseBody behavior
public @interface RestController {
}
```

**When to Use Each:**

**Use @Controller When:**
- Building traditional web applications
- Returning HTML views
- Using server-side rendering
- Working with template engines

**Use @RestController When:**
- Building REST APIs
- Returning JSON/XML data
- Creating microservices
- Building backend for SPA (React, Angular, Vue)

**Mixed Usage Example:**

```java
@Controller
@RequestMapping("/mixed")
public class MixedController {
    
    // Returns HTML view
    @GetMapping("/page")
    public String showPage(Model model) {
        model.addAttribute("data", "Some data");
        return "mypage";
    }
    
    // Returns JSON (needs @ResponseBody)
    @GetMapping("/data")
    @ResponseBody
    public Map<String, Object> getData() {
        return Map.of("key", "value");
    }
    
    // Returns HTML view
    @GetMapping("/form")
    public String showForm() {
        return "user-form";
    }
    
    // Handles form submission, redirects to view
    @PostMapping("/form")
    public String handleForm(@ModelAttribute User user) {
        userService.save(user);
        return "redirect:/mixed/success";
    }
}
```

**Serialization/Deserialization:**

With @RestController, Spring Boot uses Jackson by default:

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        Product product = new Product(1L, "Laptop", 999.99);
        return product;
        // Jackson automatically converts to:
        // {"id":1,"name":"Laptop","price":999.99}
    }
    
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        // Jackson automatically converts JSON request body to Product object
        return productService.save(product);
    }
}
```

**Best Practice:**
- Use **@RestController** for all REST API controllers (cleaner, less boilerplate)
- Use **@Controller** for traditional web MVC applications
- Don't mix both paradigms in the same controller (keep them separate)

---

### Q11: Which HTTP methods are idempotent, and why is this concept important?

**Answer:**

**Idempotent Definition:**

An HTTP method is **idempotent** if making the same request multiple times has the same effect as making it once. The server state is the same whether you send the request 1 time or 100 times.

**Mathematical Analogy:**
```
f(f(x)) = f(x)

Example:
abs(abs(-5)) = abs(-5) = 5  ← Idempotent
increment(increment(5)) = 7 ≠ increment(5) = 6  ← Not idempotent
```

**Idempotent HTTP Methods:**

**1. GET - Retrieve Data**
```java
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userService.findById(id);
}
```

**Idempotent:** ✅ Yes
- **Effect:** Retrieves data, doesn't modify server state
- **Multiple calls:** Same data returned every time (assuming no other changes)
- **Side effects:** None

```bash
# All these produce same result
GET /users/123
GET /users/123
GET /users/123
```

**2. PUT - Update/Replace Resource**
```java
@PutMapping("/users/{id}")
public User updateUser(@PathVariable Long id, @RequestBody User user) {
    user.setId(id);
    return userService.update(user);
}
```

**Idempotent:** ✅ Yes
- **Effect:** Replaces entire resource with provided data
- **Multiple calls:** Resource ends up in same state
- **Example:** Setting email to "john@example.com" 10 times still results in email being "john@example.com"

```bash
PUT /users/123
{"name":"John","email":"john@example.com"}

# Calling again
PUT /users/123
{"name":"John","email":"john@example.com"}

# Result: User 123 still has name="John", email="john@example.com"
# Same end state regardless of how many times called
```

**3. DELETE - Remove Resource**
```java
@DeleteMapping("/users/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
}
```

**Idempotent:** ✅ Yes
- **Effect:** Removes resource
- **Multiple calls:** Resource is deleted (or already gone)
- **First call:** Deletes user, returns 204 No Content
- **Subsequent calls:** User already deleted, may return 404 Not Found (still idempotent)

```bash
DELETE /users/123  → 204 No Content (user deleted)
DELETE /users/123  → 404 Not Found (already deleted)
DELETE /users/123  → 404 Not Found (still deleted)

# End state: User 123 doesn't exist (same regardless of calls)
```

**4. HEAD - Get Headers Only**
```java
@RequestMapping(value = "/users/{id}", method = RequestMethod.HEAD)
public ResponseEntity<Void> checkUserExists(@PathVariable Long id) {
    boolean exists = userService.exists(id);
    return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
}
```

**Idempotent:** ✅ Yes
- Same as GET but returns only headers (no body)
- No modification of server state

**5. OPTIONS - Get Allowed Methods**

**Idempotent:** ✅ Yes
- Returns allowed HTTP methods for resource
- No modification of server state

**Non-Idempotent HTTP Methods:**

**1. POST - Create Resource**
```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody User user) {
    User created = userService.create(user);
    return ResponseEntity.created(URI.create("/users/" + created.getId()))
                        .body(created);
}
```

**Idempotent:** ❌ No
- **Effect:** Creates new resource each time
- **Multiple calls:** Creates multiple resources
- **Different end state each time**

```bash
POST /users {"name":"John"}  → Creates user with ID 1
POST /users {"name":"John"}  → Creates user with ID 2
POST /users {"name":"John"}  → Creates user with ID 3

# End state different after each call (3 users created!)
```

**2. PATCH - Partial Update**
```java
@PatchMapping("/users/{id}")
public User partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
    return userService.partialUpdate(id, updates);
}
```

**Idempotent:** ⚠️ Depends on implementation

**Can be idempotent:**
```bash
PATCH /users/123
{"email": "new@example.com"}  # Setting to specific value

# Multiple calls still result in email="new@example.com"
```

**Not idempotent:**
```bash
PATCH /users/123
{"operation": "increment", "field": "loginCount"}

# First call: loginCount = 1
# Second call: loginCount = 2
# Third call: loginCount = 3
# Different state each time!
```

**Why Idempotency Matters:**

**1. Network Reliability**
```
Client                                Server
  |--------Request------X (timeout)
  |--------Request------X (timeout)  
  |--------Request----->|  (success)
```

If idempotent, safe to retry without checking:
- GET - Safe to retry
- PUT - Safe to retry (same end state)
- DELETE - Safe to retry (item still deleted)
- POST - NOT safe to retry (would create duplicates)

**2. Automatic Retries**
```java
// Libraries like Spring Retry can safely retry idempotent operations
@Retryable(maxAttempts = 3)
@GetMapping("/external-api/data")
public Data fetchData() {
    return externalService.getData(); // Safe to retry GET
}
```

**3. Load Balancers / Proxies**
- Can safely retry idempotent requests on failure
- POST requests might be duplicated if retried

**4. Browser Refresh**
- GET requests can be refreshed safely
- POST requests cause "Resubmit form?" warning (browser knows it's dangerous)

**5. Distributed Systems**
- Idempotent operations are essential for eventual consistency
- Safe to reprocess messages from queue

**Best Practices:**

**For POST (Non-Idempotent):**

**Solution 1: Use Idempotency Keys**
```java
@PostMapping("/payments")
public Payment createPayment(@RequestBody PaymentRequest request,
                             @RequestHeader("Idempotency-Key") String idempotencyKey) {
    
    // Check if request with this key already processed
    Payment existing = paymentService.findByIdempotencyKey(idempotencyKey);
    if (existing != null) {
        return existing; // Return cached result
    }
    
    // Process new payment
    Payment payment = paymentService.process(request);
    paymentService.saveIdempotencyKey(idempotencyKey, payment);
    return payment;
}
```

**Solution 2: Return 409 Conflict for Duplicates**
```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody User user) {
    if (userService.existsByEmail(user.getEmail())) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
    User created = userService.create(user);
    return ResponseEntity.created(URI.create("/users/" + created.getId())).body(created);
}
```

**For PATCH:**

Make operations idempotent by using absolute values:
```java
// ✅ Idempotent
PATCH /users/123 {"email": "new@example.com"}

// ❌ Not idempotent  
PATCH /users/123 {"operation": "increment", "field": "age"}

// ✅ Better: Use PUT or set absolute value
PUT /users/123/age {"age": 31}
```

**Quick Reference:**

| Method | Idempotent | Safe (No Side Effects) | Use Case |
|--------|-----------|------------------------|----------|
| GET | ✅ Yes | ✅ Yes | Retrieve data |
| HEAD | ✅ Yes | ✅ Yes | Check existence |
| OPTIONS | ✅ Yes | ✅ Yes | Discover methods |
| PUT | ✅ Yes | ❌ No | Replace resource |
| DELETE | ✅ Yes | ❌ No | Remove resource |
| POST | ❌ No | ❌ No | Create resource |
| PATCH | ⚠️ Maybe | ❌ No | Partial update |

Understanding and correctly implementing idempotency is crucial for building robust, reliable REST APIs that handle network failures and retries gracefully.

---

### Q12: How do you handle error responses and custom HTTP status codes in a Spring RESTful service?

**Answer:**

Spring provides multiple ways to handle errors and return appropriate HTTP status codes in REST APIs.

**1. @ResponseStatus Annotation**

**Simple static error responses:**

```java
// Custom exception with status code
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// Usage in controller
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        // Automatically returns 404 with exception message
    }
}
```

**2. ResponseEntity for Dynamic Control**

**Full control over response:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get()); // 200 OK
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user, 
                                           UriComponentsBuilder uriBuilder) {
        User created = userService.create(user);
        
        // 201 Created with Location header
        URI location = uriBuilder.path("/api/users/{id}")
                                 .buildAndExpand(created.getId())
                                 .toUri();
        
        return ResponseEntity.created(location).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, 
                                           @RequestBody User user) {
        if (!userService.exists(id)) {
            return ResponseEntity.notFound().build(); // 404
        }
        
        User updated = userService.update(id, user);
        return ResponseEntity.ok(updated); // 200 OK
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userService.exists(id)) {
            return ResponseEntity.notFound().build(); // 404
        }
        
        userService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
```

**3. @ExceptionHandler - Method Level**

**Handle specific exceptions in controller:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    // Handle exceptions in this controller only
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
```

**4. @ControllerAdvice - Global Exception Handler**

**Handle exceptions across all controllers:**

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    // Handle specific custom exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ValidationErrorResponse response = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            errors,
            LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // Handle generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        // Log full exception
        log.error("Unexpected error", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    // Handle specific HTTP exceptions
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex) {
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Malformed JSON request",
            LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
}
```

**5. Custom Error Response Structure**

```java
@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    // Constructor for simple errors
    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
}

@Data
@AllArgsConstructor
public class ValidationErrorResponse {
    private int status;
    private String message;
    private List<String> errors;
    private LocalDateTime timestamp;
}
```

**Response Example:**
```json
{
  "timestamp": "2026-02-10T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 123",
  "path": "/api/users/123"
}
```

**6. Validation with Custom Responses**

```java
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserCreateRequest request) {
        // @Valid triggers validation
        // If fails, MethodArgumentNotValidException thrown
        // Caught by @ControllerAdvice
        
        User user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}

// Request DTO with validation
@Data
public class UserCreateRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$", 
             message = "Password must contain at least one uppercase letter and one number")
    private String password;
}
```

**Validation Error Response:**
```json
{
  "status": 400,
  "message": "Validation Failed",
  "errors": [
    "username: Username is required",
    "email: Email must be valid",
    "password: Password must be at least 8 characters"
  ],
  "timestamp": "2026-02-10T14:35:00"
}
```

**7. Problem Details (RFC 7807)**

**Spring Boot 3+ supports RFC 7807 Problem Details:**

```java
@Configuration
public class ProblemDetailsConfig {
    
    @Bean
    public ProblemDetailsExceptionHandler problemDetailsExceptionHandler() {
        return new ProblemDetailsExceptionHandler();
    }
}

// Enable in application.properties
// spring.mvc.problemdetails.enabled=true

// Custom problem detail
@ExceptionHandler(InsufficientFundsException.class)
public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.UNPROCESSABLE_ENTITY,
        ex.getMessage()
    );
    problemDetail.setTitle("Insufficient Funds");
    problemDetail.setProperty("balance", ex.getCurrentBalance());
    problemDetail.setProperty("requiredAmount", ex.getRequiredAmount());
    
    return problemDetail;
}
```

**Response (RFC 7807 format):**
```json
{
  "type": "about:blank",
  "title": "Insufficient Funds",
  "status": 422,
  "detail": "Your account balance is insufficient for this transaction",
  "balance": 50.00,
  "requiredAmount": 100.00
}
```

**8. Common HTTP Status Codes**

```java
// Success
ResponseEntity.ok(data);                        // 200 OK
ResponseEntity.status(HttpStatus.CREATED);      // 201 Created
ResponseEntity.accepted();                      // 202 Accepted
ResponseEntity.noContent();                     // 204 No Content

// Client Errors
ResponseEntity.badRequest();                    // 400 Bad Request
ResponseEntity.status(HttpStatus.UNAUTHORIZED); // 401 Unauthorized
ResponseEntity.status(HttpStatus.FORBIDDEN);    // 403 Forbidden
ResponseEntity.notFound();                      // 404 Not Found
ResponseEntity.status(HttpStatus.CONFLICT);     // 409 Conflict
ResponseEntity.unprocessableEntity();           // 422 Unprocessable Entity

// Server Errors
ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);  // 500
ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE);    // 503
```

**9. Complete Example with All Techniques**

```java
// Custom exceptions
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}

// Global exception handler
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }
    
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ResourceAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }
    
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex, HttpStatus status, HttpServletRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.status(status).body(error);
    }
}

// Controller
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserCreateRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User with email already exists");
        }
        
        User created = userService.create(request);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        
        return ResponseEntity.created(location).body(created);
    }
}
```

**Best Practices:**

1. **Use @ControllerAdvice for global error handling**
2. **Create custom exceptions for domain errors**
3. **Return consistent error response structure**
4. **Use appropriate HTTP status codes**
5. **Don't expose internal error details to clients**
6. **Log errors appropriately (especially 500s)**
7. **Include helpful error messages for clients**
8. **Use validation annotations for input validation**
9. **Return RFC 7807 Problem Details for modern APIs**

---

### Q13: What is the difference between @RequestParam, @PathVariable, and @RequestBody?

**Answer:**

These three annotations extract data from different parts of an HTTP request.

**1. @PathVariable - URL Path Parameters**

**Purpose:** Extract values from the URL path itself

**Syntax:** `/api/users/{id}/posts/{postId}`

**Example:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Single path variable
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    // Multiple path variables
    @GetMapping("/{userId}/posts/{postId}")
    public Post getUserPost(@PathVariable Long userId, 
                            @PathVariable Long postId) {
        return postService.findByUserAndId(userId, postId);
    }
    
    // Optional: Specify variable name if different from parameter
    @GetMapping("/{user-id}")
    public User getUserAlt(@PathVariable("user-id") Long userId) {
        return userService.findById(userId);
    }
    
    // Multiple path variables with Map
    @GetMapping("/{userId}/posts/{postId}/comments/{commentId}")
    public Comment getComment(@PathVariable Map<String, String> pathVars) {
        Long userId = Long.parseLong(pathVars.get("userId"));
        Long postId = Long.parseLong(pathVars.get("postId"));
        Long commentId = Long.parseLong(pathVars.get("commentId"));
        return commentService.find(userId, postId, commentId);
    }
}
```

**HTTP Requests:**
```bash
GET /api/users/123
GET /api/users/123/posts/456
GET /api/users/123/posts/456/comments/789
```

**Characteristics:**
- Part of the URL structure
- Required by default (404 if missing)
- Typically used for resource identification
- RESTful resource hierarchy

**2. @RequestParam - Query Parameters**

**Purpose:** Extract query string values

**Syntax:** `/api/users?page=1&size=10&sort=name`

**Example:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Single required parameter
    @GetMapping
    public List<User> getUsers(@RequestParam String department) {
        return userService.findByDepartment(department);
    }
    
    // Optional parameter with default value
    @GetMapping("/search")
    public List<User> searchUsers(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy) {
        
        return userService.findAll(PageRequest.of(page, size, Sort.by(sortBy)));
    }
    
    // Multiple optional parameters
    @GetMapping("/filter")
    public List<User> filterUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean active) {
        
        return userService.filter(name, email, active);
    }
    
    // Parameter name different from variable
    @GetMapping("/query")
    public List<User> query(@RequestParam("q") String searchTerm) {
        return userService.search(searchTerm);
    }
    
    // All query parameters as Map
    @GetMapping("/dynamic")
    public List<User> dynamicFilter(@RequestParam Map<String, String> allParams) {
        return userService.dynamicFilter(allParams);
    }
    
    // List/Array parameters
    @GetMapping("/by-ids")
    public List<User> getUsersByIds(@RequestParam List<Long> ids) {
        return userService.findAllById(ids);
    }
}
```

**HTTP Requests:**
```bash
GET /api/users?department=Engineering
GET /api/users/search?page=2&size=50&sortBy=name
GET /api/users/filter?name=John&active=true
GET /api/users/query?q=developer
GET /api/users/by-ids?ids=1,2,3,4,5
GET /api/users/by-ids?ids=1&ids=2&ids=3  # Alternative array format
```

**Characteristics:**
- Optional by default (can set required=true)
- Appear after `?` in URL
- Typically used for filtering, pagination, search
- Can have default values

**3. @RequestBody - HTTP Request Body**

**Purpose:** Extract and deserialize the entire request body (usually JSON)

**Example:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Simple request body
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    // With validation
    @PostMapping("/validate")
    public ResponseEntity<User> createUserWithValidation(
            @Valid @RequestBody UserCreateRequest request) {
        
        User user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    // Update with request body
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
                          @RequestBody User user) {
        user.setId(id);
        return userService.update(user);
    }
    
    // Partial update with Map
    @PatchMapping("/{id}")
    public User partialUpdate(@PathVariable Long id,
                              @RequestBody Map<String, Object> updates) {
        return userService.partialUpdate(id, updates);
    }
    
    // Complex nested objects
    @PostMapping("/orders")
    public Order createOrder(@RequestBody OrderRequest request) {
        return orderService.create(request);
    }
}

// DTO for request body
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    
    @NotBlank
    private String username;
    
    @Email
    private String email;
    
    @Size(min = 8)
    private String password;
    
    private Address address;
    private List<String> roles;
}

@Data
public class OrderRequest {
    private Long customerId;
    private List<OrderItem> items;
    private PaymentInfo payment;
    private ShippingAddress shipping;
}
```

**HTTP Requests:**
```bash
POST /api/users
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "address": {
    "street": "123 Main St",
    "city": "New York",
    "zipCode": "10001"
  },
  "roles": ["USER", "ADMIN"]
}
```

**Characteristics:**
- Required by default
- Entire request body deserialized to object
- Uses HttpMessageConverter (Jackson for JSON)
- Typically used for POST, PUT, PATCH
- Can only be used once per method (can't have multiple @RequestBody)

**4. Combining All Three**

You can use all three in the same method:

```java
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
    
    @PutMapping("/{orgId}/users/{userId}")
    public User updateUserInOrganization(
            @PathVariable Long orgId,                    // From URL path
            @PathVariable Long userId,                   // From URL path
            @RequestParam(defaultValue = "false") boolean sendNotification, // From query string
            @RequestBody UserUpdateRequest request) {    // From request body
        
        User updated = userService.updateUserInOrg(orgId, userId, request);
        
        if (sendNotification) {
            notificationService.send(updated);
        }
        
        return updated;
    }
}
```

**HTTP Request:**
```bash
PUT /api/organizations/10/users/25?sendNotification=true
Content-Type: application/json

{
  "email": "newemail@example.com",
  "department": "Engineering"
}
```

**Comparison Table:**

| Feature | @PathVariable | @RequestParam | @RequestBody |
|---------|--------------|---------------|--------------|
| Location | URL path | Query string | Request body |
| Syntax | `/users/{id}` | `/users?id=123` | JSON in body |
| Required | Yes (by default) | No (by default) | Yes (by default) |
| Default Value | ❌ No | ✅ Yes | ❌ No |
| Multiple Values | Multiple variables | Multiple parameters or List | Single object |
| Use Case | Resource ID | Filtering, pagination | Create/Update data |
| HTTP Methods | Any | Usually GET | POST, PUT, PATCH |
| Data Type | Simple types | Simple types, List | Complex objects |
| Validation | ✅ Yes | ✅ Yes | ✅ Yes (@Valid) |

**5. Edge Cases and Best Practices**

**Optional Path Variables (Spring 4.3.3+):**
```java
@GetMapping({"/users", "/users/{id}"})
public Object getUsers(@PathVariable(required = false) Long id) {
    if (id != null) {
        return userService.findById(id);
    }
    return userService.findAll();
}
```

**Matrix Variables (rarely used):**
```java
// Enable in config
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setRemoveSemicolonContent(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }
}

// Usage
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id,
                   @MatrixVariable(required = false) String  version) {
    // URL: /users/123;version=2.0
}
```

**Best Practices:**
1. **Use @PathVariable** for resource identification (IDs)
2. **Use @RequestParam** for optional filtering/pagination
3. **Use @RequestBody** for data creation/updates
4. **Validate all inputs** with @Valid or custom validators
5. **Use DTOs** instead of domain entities in @RequestBody
6. **Provide default values** for @RequestParam where appropriate
7. **Keep URLs clean** - prefer path variables over query params for core resource identification

---

(Continuing with remaining questions...)

### Q14: How do you implement API versioning (e.g., URI versioning, header versioning)?

**Answer:**

API versioning ensures backward compatibility when APIs evolve. There are several strategies:

**1. URI Path Versioning (Most Common)**

Version is part of the URL path.

```java
// Version 1
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller {
    
    @GetMapping("/{id}")
    public UserV1Response getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserV1Response(user.getId(), user.getName());
    }
}

// Version 2 - Added email field
@RestController
@RequestMapping("/api/v2/users")
public class UserV2Controller {
    
    @GetMapping("/{id}")
    public UserV2Response getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserV2Response(user.getId(), user.getName(), user.getEmail());
    }
}
```

**HTTP Requests:**
```bash
GET /api/v1/users/123  # Old version
GET /api/v2/users/123  # New version
```

**Pros:**
- Clear and explicit
- Easy to understand
- Easy to route
- Cacheable

**Cons:**
- URL changes with version
- Duplicate code/controllers

**2. Request Parameter Versioning**

Version passed as query parameter.

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping(value = "/{id}", params = "version=1")
    public UserV1Response getUserV1(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserV1Response(user.getId(), user.getName());
    }
    
    @GetMapping(value = "/{id}", params = "version=2")
    public UserV2Response getUserV2(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserV2Response(user.getId(), user.getName(), user.getEmail());
    }
    
    // Default to latest version
    @GetMapping("/{id}")
    public UserV2Response getUser(@PathVariable Long id) {
        return getUserV2(id);
    }
}
```

**HTTP Requests:**
```bash
GET /api/users/123?version=1
GET /api/users/123?version=2
GET /api/users/123  # Defaults to v2
```

**3. Header Versioning (Accept Header)**

Version specified in HTTP header.

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping(value = "/{id}", headers = "API-Version=1")
    public UserV1Response getUserV1(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserV1Response(user.getId(), user.getName());
    }
    
    @GetMapping(value = "/{id}", headers = "API-Version=2")
    public UserV2Response getUserV2(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserV2Response(user.getId(), user.getName(), user.getEmail());
    }
}
```

**HTTP Requests:**
```bash
curl -H "API-Version: 1" http://api.example.com/api/users/123
curl -H "API-Version: 2" http://api.example.com/api/users/123
```

**4. Content Negotiation (Media Type Versioning)**

Version in Accept header using custom media types.

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping(value = "/{id}", produces = "application/vnd.company.api.v1+json")
    public UserV1Response getUserV1(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserV1Response(user.getId(), user.getName());
    }
    
    @GetMapping(value = "/{id}", produces = "application/vnd.company.api.v2+json")
    public UserV2Response getUserV2(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserV2Response(user.getId(), user.getName(), user.getEmail());
    }
}
```

**HTTP Requests:**
```bash
curl -H "Accept: application/vnd.company.api.v1+json" http://api.example.com/api/users/123
curl -H "Accept: application/vnd.company.api.v2+json" http://api.example.com/api/users/123
```

**5. Custom Header Strategy**

Custom implementation with interceptor:

```java
// Custom annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    int value();
}

// Controller
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @ApiVersion(1)
    @GetMapping("/{id}")
    public UserV1Response getUserV1(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserV1Response(user.getId(), user.getName());
    }
    
    @ApiVersion(2)
    @GetMapping("/{id}")
    public UserV2Response getUserV2(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserV2Response(user.getId(), user.getName(), user.getEmail());
    }
}

// Interceptor to route based on version
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response,
                             Object handler) throws Exception {
        
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            ApiVersion apiVersion = method.getMethodAnnotation(ApiVersion.class);
            
            if (apiVersion != null) {
                String requestedVersion = request.getHeader("X-API-Version");
                if (requestedVersion == null) {
                    requestedVersion = "2"; // Default to latest
                }
                
                int version = Integer.parseInt(requestedVersion);
                if (version != apiVersion.value()) {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return false;
                }
            }
        }
        return true;
    }
}
```

**Best Practices:**

1. **Choose one strategy and stick with it**
2. **URI versioning is most common and easiest**
3. **Major versions only** (v1, v2, v3, not v1.1, v1.2)
4. **Deprecate old versions gradually**
5. **Document version differences**
6. **Support old versions for reasonable time**
7. **Use semantic versioning for libraries**

**Comparison:**

| Strategy | Pros | Cons | Use When |
|----------|------|------|----------|
| URI Path | Clear, cacheable, simple | URL pollution | Public APIs |
| Query Param | Flexible | Less REST-like | Internal APIs |
| Header | Clean URLs | Less discoverable | Advanced clients |
| Media Type | RESTful | Complex | Academic purity |

**Recommendation:** Use **URI path versioning** for simplicity and clarity.

---

### Q15: Describe the flow of an HTTP request from the client to the database and back through a Spring Boot application.

**Answer:**

Here's the complete request/response flow:

```
[Client] ➜ [HTTP Request] ➜ [Embedded Server] ➜ [Filter Chain] ➜ 
[DispatcherServlet] ➜ [Handler Mapping] ➜ [Interceptors] ➜ 
[Controller] ➜ [Service] ➜ [Repository] ➜ [JPA/Hibernate] ➜ 
[JDBC] ➜ [Database]
```

**Detailed Flow:**

**1. Client Sends HTTP Request**
```bash
POST /api/users HTTP/1.1
Host: example.com
Content-Type: application/json
Authorization: Bearer <token>

{"name":"John Doe","email":"john@example.com"}
```

**2. Embedded Server (Tomcat/Jetty) Receives Request**
- Listens on port (default 8080)
- Creates HttpServletRequest and HttpServletResponse objects
- Passes to filter chain

**3. Filter Chain Execution (Before Controller)**

Filters execute in order:

```java
// a) Logging Filter
@Component
@Order(1)
public class LoggingFilter implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        log.info("Request: {} {}", request.getMethod(), request.getRequestURI());
        chain.doFilter(request, response); // Continue chain
    }
}

// b) Security Filter (Spring Security)
// - Validates JWT token
// - Sets SecurityContext
// - Checks authentication

// c) CORS Filter
// - Validates origin
// - Adds CORS headers

// d) Character Encoding Filter
// - Sets UTF-8 encoding
```

**4. DispatcherServlet (Front Controller)**

Spring's central servlet:
- Receives request from filters
- Coordinates entire request processing
- Delegates to various components

```java
// Simplified DispatcherServlet logic
public class DispatcherServlet extends HttpServlet {
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) {
        // 1. Find handler (controller method)
        HandlerExecutionChain handler = getHandler(request);
        
        // 2. Get adapter for handler
        HandlerAdapter adapter = getHandlerAdapter(handler);
        
        // 3. Execute interceptors (pre-process)
        handler.applyPreHandle(request, response);
        
        // 4. Invoke controller method
        ModelAndView mv = adapter.handle(request, response, handler);
        
        // 5. Execute interceptors (post-process)
        handler.applyPostHandle(request, response, mv);
        
        // 6. Process result (render view or serialize to JSON)
        processDispatchResult(request, response, mv);
    }
}
```

**5. Handler Mapping**

Finds which controller method should handle the request:

```java
@Component
public class RequestMappingHandlerMapping {
    public HandlerMethod getHandler(HttpServletRequest request) {
        // Match request to @RequestMapping annotations
        // POST /api/users → UserController.createUser()
        return handlerMethod;
    }
}
```

**6. Handler Interceptors (Pre-Processing)**

Execute before controller:

```java
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // Check permissions
        // Log request
        // Measure time
        return true; // Continue to controller
    }
}
```

**7. Argument Resolvers**

Convert request data to method parameters:

```java
// Resolves @PathVariable, @RequestParam, @RequestBody
@RestController
public class UserController {
    @PostMapping("/users")
    public User createUser(@RequestBody User user) { // ← ArgumentResolver converts JSON to User
        // ...
    }
}
```

**HttpMessageConverter** deserializes JSON:
```java
// Jackson converts JSON to Java object
{
  "name": "John Doe",
  "email": "john@example.com"
}
↓
User user = new User("John Doe", "john@example.com");
```

**8. Controller Method Execution**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserRequest request) {
        // Validation happens here (@Valid)
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

**9. Service Layer Execution**

Business logic layer:

```java
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    public User createUser(UserRequest request) {
        // Business logic
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        
        // Start database transaction
        User saved = userRepository.save(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(saved);
        
        // Transaction commits here
        return saved;
    }
}
```

**10. Repository Layer**

Data access:

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}

// Spring Data JPA generates implementation:
public class UserRepositoryImpl implements UserRepository {
    public User save(User user) {
        return entityManager.persist(user);
    }
}
```

**11. JPA/Hibernate Layer**

ORM processing:

```java
// Hibernate session
Session session = entityManager.unwrap(Session.class);

// 1. Persist entity
session.persist(user);

// 2. Hibernate generates SQL
// INSERT INTO users (name, email) VALUES (?, ?)

// 3. Flush to database
session.flush();

// 4. Add to persistence context (L1 cache)
// 5. Generate ID (if auto-increment)
```

**12. JDBC Layer**

Database connectivity:

```java
// DataSource provides connection
Connection conn = dataSource.getConnection();

// PreparedStatement executes SQL
PreparedStatement stmt = conn.prepareStatement(
    "INSERT INTO users (name, email) VALUES (?, ?)",
    Statement.RETURN_GENERATED_KEYS
);
stmt.setString(1, "John Doe");
stmt.setString(2, "john@example.com");

// Execute
int rows = stmt.executeUpdate();

// Get generated ID
ResultSet rs = stmt.getGenerated Keys();
if (rs.next()) {
    Long id = rs.getLong(1);
}
```

**13. Database Execution**

```sql
-- MySQL/PostgreSQL executes:
INSERT INTO users (name, email, created_at)
VALUES ('John Doe', 'john@example.com', NOW());

-- Returns generated ID: 123
```

**14. Response Journey Back**

**Database → JDBC:**
- Returns result set with generated ID

**JDBC → Hibernate:**
- Maps result to User entity
- Sets ID on entity

**Hibernate → Repository:**
- Returns managed entity
- Entity is now in persistence context

**Repository → Service:**
- Returns saved user with ID

**Service → Controller:**
- Completes transaction (commit)
- Returns user to controller

**Controller → Argument Resolver:**
- Returns ResponseEntity with User

**HttpMessageConverter (Serialization):**
```java
// Jackson converts User object to JSON
User user = new User(123L, "John Doe", "john@example.com");
↓
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2026-02-10T14:30:00"
}
```

**15. Interceptors (Post-Processing)**

```java
@Override
public void postHandle(HttpServletRequest request,
                       HttpServletResponse response,
                       Object handler,
                       ModelAndView modelAndView) {
    // Log response
    // Add custom headers
    long duration = System.currentTimeMillis() - startTime;
    response.addHeader("X-Response-Time", duration + "ms");
}
```

**16. DispatcherServlet Sends Response**

Sets HTTP response:
```
HTTP/1.1 201 Created
Content-Type: application/json
Location: /api/users/123
X-Response-Time: 45ms

{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2026-02-10T14:30:00"
}
```

**17. Filter Chain (After Controller)**

Filters execute in reverse order:
```java
@Override
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    try {
        chain.doFilter(request, response); // Controller executed
    } finally {
        // Cleanup
        // Log response
        // Remove thread-local variables
    }
}
```

**18. Embedded Server Sends Response**

Tomcat writes response to socket and sends to client.

**19. Client Receives Response**

```javascript
// JavaScript client
const response = await fetch('http://example.com/api/users', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({name: 'John Doe', email: 'john@example.com'})
});

const user = await response.json();
console.log(user.id); // 123
```

**Complete Architecture Diagram:**

```
┌─────────────────────────────────────────────────┐
│                    CLIENT                        │
│  (Browser, Mobile App, Another Service)         │
└────────────────────┬────────────────────────────┘
                     │ HTTP Request
                     ▼
┌─────────────────────────────────────────────────┐
│           EMBEDDED SERVER (Tomcat)               │
│                  Port 8080                       │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│              FILTER CHAIN                        │
│  ┌──────────────────────────────────────────┐   │
│  │ 1. Logging Filter                        │   │
│  │ 2. Security Filter (JWT Validation)      │   │
│  │ 3. CORS Filter                           │   │
│  │ 4. Character Encoding Filter             │   │
│  └──────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│          DISPATCHER SERVLET                      │
│        (Front Controller Pattern)                │
└────────────────────┬────────────────────────────┘
                     │
            ┌────────┴────────┐
            │                 │
            ▼                 ▼
    ┌──────────────┐   ┌──────────────┐
    │   Handler    │   │   Handler    │
    │   Mapping    │   │   Adapter    │
    └──────┬───────┘   └──────┬───────┘
           │                  │
           └────────┬─────────┘
                    ▼
        ┌───────────────────────┐
        │   INTERCEPTORS        │
        │   (Pre-Processing)    │
        └───────────┬───────────┘
                    │
                    ▼
        ┌───────────────────────┐
        │  CONTROLLER LAYER     │
        │  @RestController      │
        │  ┌─────────────────┐  │
        │  │ UserController  │  │
        │  └────────┬────────┘  │
        └───────────┼───────────┘
                    │
                    ▼
        ┌───────────────────────┐
        │   SERVICE LAYER       │
        │   @Service            │
        │  ┌─────────────────┐  │
        │  │  UserService    │  │
        │  │  @Transactional │  │
        │  └────────┬────────┘  │
        └───────────┼───────────┘
                    │
                    ▼
        ┌───────────────────────┐
        │  REPOSITORY LAYER     │
        │  @Repository          │
        │  ┌─────────────────┐  │
        │  │ UserRepository  │  │
        │  │ (Spring Data)   │  │
        │  └────────┬────────┘  │
        └───────────┼───────────┘
                    │
                    ▼
        ┌───────────────────────┐
        │    JPA/HIBERNATE      │
        │  ┌─────────────────┐  │
        │  │ EntityManager   │  │
        │  │ Session         │  │
        │  │ L1 Cache        │  │
        │  └────────┬────────┘  │
        └───────────┼───────────┘
                    │
                    ▼
        ┌───────────────────────┐
        │       JDBC            │
        │  ┌─────────────────┐  │
        │  │  DataSource     │  │
        │  │  Connection     │  │
        │  │  PreparedStmt   │  │
        │  └────────┬────────┘  │
        └───────────┼───────────┘
                    │
                    ▼
        ┌───────────────────────┐
        │      DATABASE         │
        │  (MySQL/PostgreSQL)   │
        └───────────────────────┘
```

This flow ensures separation of concerns, maintainability, and scalability in Spring Boot applications.

---

## Data Testing and Persistence

(Continuing in next message due to length...)

### Q16: What is Spring Data JPA, and how does it simplify data access compared to traditional JPA/Hibernate?

**Answer:**

Spring Data JPA is a Spring module that simplifies database access by reducing boilerplate code and providing powerful abstractions over JPA.

**Traditional JPA/Hibernate Approach:**

```java
@Repository
public class UserRepositoryImpl {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }
    
    public List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM User u", User.class)
                           .getResultList();
    }
    
    public List<User> findByEmail(String email) {
        return entityManager.createQuery(
            "SELECT u FROM User u WHERE u.email = :email", User.class)
            .setParameter("email", email)
            .getResultList();
    }
    
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        } else {
            return entityManager.merge(user);
        }
    }
    
    public void delete(User user) {
        entityManager.remove(
            entityManager.contains(user) ? user : entityManager.merge(user)
        );
    }
    
    public void deleteById(Long id) {
        User user = findById(id);
        if (user != null) {
            delete(user);
        }
    }
    
    public long count() {
        return entityManager.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                           .getSingleResult();
    }
    
    public boolean existsById(Long id) {
        return findById(id) != null;
    }
    
   // Many more boilerplate methods...
}
```

**Problems:**
- Repetitive CRUD code for every entity
- Manual query writing
- Error-prone (typos in queries)
- More code to test and maintain

**Spring Data JPA Approach:**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // That's it! All basic CRUD methods already available:
    // - save(User)
    // - findById(Long)
    // - findAll()
    // - delete(User)
    // - deleteById(Long)
    // - count()
    // - existsById(Long)
    // And many more...
}
```

**Key Simplifications:**

**1. No Implementation Needed**

Spring Data JPA generates implementation at runtime:

```java
// You write only the interface
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA creates the implementation class automatically
}

// Usage
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository; // Injected proxy implementation
    
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
```

**2. Query Methods from Method Names**

Spring Data JPA derives queries from method names:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // SELECT u FROM User u WHERE u.email = ?1
    List<User> findByEmail(String email);
    
    // SELECT u FROM User u WHERE u.name = ?1 AND u.active = ?2
    List<User> findByNameAndActive(String name, boolean active);
    
    // SELECT u FROM User u WHERE u.age > ?1
    List<User> findByAgeGreaterThan(int age);
    
    // SELECT u FROM User u WHERE u.name LIKE %?1%
    List<User> findByNameContaining(String keyword);
    
    // SELECT u FROM User u WHERE u.email = ?1
    Optional<User> findByEmail(String email);
    
    // SELECT COUNT(u) FROM User u WHERE u.active = ?1
    long countByActive(boolean active);
    
    // DELETE FROM User u WHERE u.email = ?1
    void deleteByEmail(String email);
    
    // Check existence
    boolean existsByEmail(String email);
}
```

**Supported Keywords:**

| Keyword | Example | JPQL snippet |
|---------|---------|--------------|
| And | findByNameAndEmail | WHERE u.name = ?1 AND u.email = ?2 |
| Or | findByNameOrEmail | WHERE u.name = ?1 OR u.email = ?2 |
| Is, Equals | findByName | WHERE u.name = ?1 |
| Between | findByAgeBetween | WHERE u.age BETWEEN ?1 AND ?2 |
| LessThan | findByAgeLessThan | WHERE u.age < ?1 |
| GreaterThan | findByAgeGreaterThan | WHERE u.age > ?1 |
| After, Before | findByCreatedAfter | WHERE u.created > ?1 |
| IsNull | findByEmailIsNull | WHERE u.email IS NULL |
| IsNotNull, NotNull | findByEmailIsNotNull | WHERE u.email IS NOT NULL |
| Like | findByNameLike | WHERE u.name LIKE ?1 |
| NotLike | findByNameNotLike | WHERE u.name NOT LIKE ?1 |
| StartingWith | findByNameStartingWith | WHERE u.name LIKE ?1% |
| EndingWith | findByNameEndingWith | WHERE u.name LIKE %?1 |
| Containing | findByNameContaining | WHERE u.name LIKE %?1% |
| OrderBy | findByAgeOrderByNameDesc | WHERE u.age = ?1 ORDER BY u.name DESC |
| Not | findByActiveNot | WHERE u.active <> ?1 |
| In | findByAgeIn(Collection ages) | WHERE u.age IN ?1 |
| NotIn | findByAgeNotIn | WHERE u.age NOT IN ?1 |
| True | findByActiveTrue | WHERE u.active = true |
| False | findByActiveFalse | WHERE u.active = false |
| IgnoreCase | findByNameIgnoreCase | WHERE UPPER(u.name) = UPPER(?1) |

**3. Custom Queries with @Query**

For complex queries:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // JPQL query
    @Query("SELECT u FROM User u WHERE u.department = :dept AND u.salary > :minSalary")
    List<User> findHighEarners(@Param("dept") String department, 
                               @Param("minSalary") BigDecimal minSalary);
    
    // Native SQL query
    @Query(value = "SELECT * FROM users WHERE DATE_PART('year', created_at) = :year",
           nativeQuery = true)
    List<User> findUsersCreatedInYear(@Param("year") int year);
    
    // Update query
    @Modifying
    @Query("UPDATE User u SET u.active = false WHERE u.lastLogin < :date")
    int deactivateInactiveUsers(@Param("date") LocalDateTime date);
    
    // DTO projection
    @Query("SELECT new com.example.dto.UserSummary(u.id, u.name, u.email) FROM User u")
    List<UserSummary> findAllSummaries();
}
```

**4. Pagination and Sorting**

Built-in support:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Pageable parameter
    Page<User> findByDepartment(String department, Pageable pageable);
    
    // Sort parameter
    List<User> findByActive(boolean active, Sort sort);
}

// Usage
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public Page<User> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return userRepository.findAll(pageable);
    }
    
    public List<User> getActiveUsersSorted() {
        Sort sort = Sort.by("lastName").ascending()
                       .and(Sort.by("firstName").ascending());
        return userRepository.findByActive(true, sort);
    }
}
```

**5. Specifications for Dynamic Queries**

Type-safe dynamic queries:

```java
public interface UserRepository extends JpaRepository<User, Long>, 
                                        JpaSpecificationExecutor<User> {
}

// Specifications
public class UserSpecifications {
    
    public static Specification<User> hasName(String name) {
        return (root, query, cb) -> cb.equal(root.get("name"), name);
    }
    
    public static Specification<User> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }
    
    public static Specification<User> inDepartment(String department) {
        return (root, query, cb) -> cb.equal(root.get("department"), department);
    }
}

// Usage
@Service
public class UserService {
    
    public List<User> searchUsers(String name, String department, Boolean active) {
        Specification<User> spec = Specification.where(null);
        
        if (name != null) {
            spec = spec.and(UserSpecifications.hasName(name));
        }
        if (department != null) {
            spec = spec.and(UserSpecifications.inDepartment(department));
        }
        if (active != null && active) {
            spec = spec.and(UserSpecifications.isActive());
        }
        
        return userRepository.findAll(spec);
    }
}
```

**6. Auditing Support**

Automatic tracking of creation/modification:

```java
@EntityListeners(AuditingEntityListener.class)
@Entity
public class User {
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @CreatedBy
    private String createdBy;
    
    @LastModifiedBy
    private String lastModifiedBy;
}

// Enable auditing
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return Optional.ofNullable(auth).map(Authentication::getName);
        };
    }
}
```

**7. Projections (Select Specific Fields)**

```java
// Interface projection
public interface UserNameOnly {
    String getName();
    String getEmail();
}

public interface UserRepository extends JpaRepository<User, Long> {
    List<UserNameOnly> findByDepartment(String department);
}

// Class-based projection (DTO)
@Value
public class UserSummary {
    Long id;
    String name;
    String email;
}

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT new com.example.dto.UserSummary(u.id, u.name, u.email) FROM User u")
    List<UserSummary> findAllSummaries();
}
```

**Comparison Summary:**

| Feature | Traditional JPA | Spring Data JPA |
|---------|----------------|-----------------|
| Implementation | Manual | Auto-generated |
| CRUD Methods | Write all manually | Inherited from JpaRepository |
| Simple Queries | EntityManager.createQuery() | Method name derivation |
| Complex Queries | @NamedQuery or JPQL | @Query annotation |
| Pagination | Manual PageImpl creation | Built-in Pageable |
| Sorting | ORDER BY in JPQL | Built-in Sort |
| Dynamic Queries | Criteria API (verbose) | Specifications (cleaner) |
| Auditing | Manual @PrePersist | @CreatedDate, @LastModifiedDate |
| Testing | Mock EntityManager | Use Spring Data test slices |
| Boilerplate Code | High | Minimal |

**Spring Data JPA saves approximately 70-80% of data access code!**

---

(Due to length constraints, I'll continue with remaining questions in a structured format. The complete guide is being created...)

Let me continue creating the complete interview guide now:
