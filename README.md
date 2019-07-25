# MicroService
Microservice AOP integrated with Spring boot and Consul

Simplified Spring Boot and Consul configuration and created AOP for domain, service and repository.


# Feature

Boot Spring from remote or local Cosul.
Focus on domain model and services.

# Getting Started
Use the example as a quick start.

## Include dependency:

<dependencies>
	<dependency>
		<groupId>com.cheeray.ms</groupId>
		<artifactId>core</artifactId>
	</dependency>
</dependencies>

## Define a domain model:
```java
  public class Product implements DomainObject {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private int stock;
  ...
  }
```
## Create a repository for the model:
```java
  @Repository("products")
  public class ProductRepo extends NamedParameterJdbcDaoSupport
      implements IRepository<Product> {

    @Autowired
    public ProductRepo(DataSource dataSource) {
      super();
      setDataSource(dataSource);
    }

    @Override
    @Loggable
    public Product get(Long id) {
      return getJdbcTemplate().queryForObject("SELECT * FROM products WHERE id=" + id,
          (rs, rowNum) -> new Product(rs.getLong("id"), rs.getString("name"),
              rs.getInt("stock")));
    }
  ...
  }
  ```
## Create a Restful controller:
  ```java
  @Controller
  public class RestController {

    @Autowired
    ProductRepo products;

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable long id) {
      return ResponseEntity.ok(products.get(id));
    }
  }
  ```
## Define Consul configuration
Create a Consul configuration file: bootstrap.yaml.
Example:
  ```yaml
spring:
  cloud:
    consul:
      host: localhost
      port: 80
      config:
        enabled: true
        failFast: true
        format: YAML
        prefix: config/service
        defaultContext: ms-example
        profileSeparator: '::'
        data-key: test
  ```

## Define configuration on Consul
Save the Spring boot configuration on Consul as a key value pair.
For example, save the content of configuration file example.yaml at "/config/service/ms-example" with key: "test".

```yaml
server:
  port: 8080 
  session-timeout: 5000
  servlet:
     context-path: /api
logging:
  level:
    org:
      springframework:
        boot:
          autoconfigure: TRACE
spring:
  application:
    name: MsApp-example
  datasource:
    continue-on-error: false
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password: password
  h2:
    console:
      enabled: true
      path: /h2
      settings:
        trace: false
        web-allow-others: false
  hateoas:
    use-hal-as-default-json-media-type: true
  http:
    converters:
      preferred-json-mapper: jackson
  cloud:
    consul:
      discovery:
        hostname: localhost
        healthCheckPath: ${server.servlet.context-path}/actuator/health
        healthCheckInterval: 30s
        tags: service=orders
        instanceId: ${spring.application.name}-${spring.cloud.consul.config.data-key}
        catalogServicesWatchDelay: 86400000
  ```
# Start service
Run java application with main class "com.cheeray.ms.MicroService" and argument "start".
