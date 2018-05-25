本学习教程尽量使用JUnit测试来验证实践过程。

## 测试代码位置

所有的测试代码位于`${project.base}/src/test/java`目录下，其中新建的Spring Starter Project会自动为我们创建一个测试类

## 测试依赖

测试需要引入的依赖为`spring-boot-starter-test`，以maven为例

``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## 测试注解

一般测试类都需要加以下两个注解
- @RunWith(SpringRunner.class)
- @SpringBootTest(webEnvironment = WebEnvironment.MOCK)


### 测试环境说明

WebEnvironment预置了4种环境

1. NONE: 不提供任何servlet环境，即使你本机中已经运行了一个真实的Sprint Boot实例，它仅使用`SpringApplication`加载一个`ApplicationContext`
2. MOCK: 加载的是`WebApplicationContext`，并且模拟一个servlet环境，一般配置`@AutoConfigureMockMvc`注解一块使用
3. DEFINED_PORT: 加载的是`ServletWebServerApplicationContext`, 提供的是真实的servlet环境，使用的是配置的`server.port`作为端口号
4. RANDOM_PORT: 同`DEFINED_PORT`，不过端口号是随机的，建议在测试中使用，可以避免因端口号冲突引发的问题

### 其它注解

- @AutoConfigureMockMvc，在测试类中使用，有了它，你就能愉快地使用`@Autowired MockMvc mvc`了

## 参考

[Sprint GS Test the Web Layer]

[Spring Test Doc]

[Sprint GS Test the Web Layer]: https://spring.io/guides/gs/testing-web/
[Spring Test Doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
