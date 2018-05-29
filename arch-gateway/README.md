## arch-gateway

网关，作为微服务系统的入口，相当于门面设计模式中的门面，所有的外部请求必须经过网关进行路由或转发。所以它非常重要，因为是入口，所以可以在它上面做访问控制，权限认证，日志记录等公共业务。Spring使用Netflix的Zuul作为网关（Spring自己也搞了个Gateway，我尝试了一下，感觉不好用）

在上一章（第二个微服务）中，讲到了ribbon负载均衡，其实，Spring Cloud的Zuul网关就已经内置了ribbon，不仅如此，还内置了Hystrix熔断器（这个熔断器，在第5章再讲），当然了，做为网关，最重要的还是路由及转发功能了。Spring官网上Spring Cloud中的Getting Started Guides有Config, Registry, Breakers, Load Balancing, Routing中后面三大块，Zuul已经给我们做好了。

### 创建工程

New Spring Boot Starter Project -> arch-gateway

https://start.spring.io/starter.zip?name=arch-gateway&groupId=com.example&artifactId=arch-gateway&version=0.0.1-SNAPSHOT&description=MS+Gateway&packageName=com.example.arch.gateway&type=maven-project&packaging=jar&javaVersion=1.8&language=java&bootVersion=2.0.1.RELEASE&dependencies=cloud-ribbon&dependencies=cloud-zuul&dependencies=cloud-feign&dependencies=cloud-starter-sleuth&dependencies=cloud-starter-zipkin
 
### 配置
 
application.properties->application.yml

```yml
server:
  port: 80
  
spring:
  application:
    name: gateway

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
        
zuul:
  force-original-query-string-encoding: true
```

修改ArchGatewayApplication.java源文件，在类上添加`@EnableZuulProxy`和`@EnableDiscoveryClient`开启网关和Eureka Client功能。

如此，一个最小化配置的网关便可以正常工作了。

### 高级配置

详细请参官方[router_and_filter_zuul]文档，这里只讲部分。

#### 超时

超时对于刚入门Zuul的朋友来说是一个非常常见的问题（本机测试没问题，一旦部署，发现好多java.net.SocketTimeoutException: Read timed out），Zuul中有三种超时

- 配置了url的路由超时，其实这种方式，相当于转发了，配置项为`zuul.host.connect-timeout-millis`和`zuul.host.connect-timeout-millis`
- 微服务路由超时，因为微服务需要走ribbon负载均衡，需要配置ribbon超时，为简单起见，不对微服务单独配置，只配置一个全局的，配置项为`ribbon.SocketTimeout`和`ribbon.ReadTimeout`。
- 熔断器超时，如果微服务不存在，则无法成功路由，此时熔断器便会起作用，默认的熔断器超时有一个公式计算，大约为`ribbon.SocketTimeout+ribbon.ReadTimeout * 4`，也可以通过`hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds`来配置。

注：后两种配置项，yml中没有联想，感觉怪怪的哈。


#### 路由配置

如果是微服务，不用显式配置，POM中可以添加`spring-boot-starter-actuator`依赖，并配置management，通过访问`/actuator/routes`查看路由列表。

```yml
management:
  endpoints:
    web:
      exposure:
        include: "*"
        exclude:
        - env
```

注：`management.endpoints.exposure`要排除env，在2.0.x版本上，包含env有可能会报`Found multiple extensions for the endpoint bean environmentEndpoint (environmentEndpointWebExtension, environmentWebEndpointExtension)`异常，导致无法启动。


### 附加功能

在实际项目应用中，本人将权限控制放在OAuth2微服务中，并且在网关上添加了诸如日志记录，参数校验，动态路由（修改zuul的路由规则）等许多功能。下面仅介绍与公司业务无关的一些附加功能。

#### 集成Swagger2

pom中添加依赖`io.springfox:springfox-swagger2:2.8.0`和`io.springfox:springfox-swagger-ui:2.8.0`

```xml
		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger2</artifactId>
			<version>2.8.0</version>
		</dependency>
		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger-ui</artifactId>
			<version>2.8.0</version>
		</dependency>
```

创建`Swagger2Configuration.java`在里面配置swagger2。详细请参考[Springfox Swagger  Doc]

#### 跨域
除参考源代码中的`ZuulConfiguration.java`

```java
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
```

### 参考

- [router_and_filter_zuul]
- [Springfox Swagger  Doc]

[router_and_filter_zuul]: http://cloud.spring.io/spring-cloud-static/Edgware.SR3/single/spring-cloud.html#_router_and_filter_zuul
[Springfox Swagger  Doc]: http://springfox.github.io/springfox/docs/current/#springfox-spring-mvc-and-spring-boot