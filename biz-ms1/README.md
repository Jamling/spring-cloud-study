## 第一个微服务

### 创建工程

新建一个名为`biz-ms1`的Spring Starter Project

Project Dependencies 选择Web->Web, Cloud -> Eureka Discovery	

   PS: 建议每个工程都带上`spring-boot-starter-web`和`spring-boot-starter-actuator`依赖	
  
```xml
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
	</dependencies>
```

配置	
  
  application.properties => application.yml

```yml
spring:
  application:
    name: biz-ms1 # 微服务的名字, 所有的客户端都可以通过http://biz-ms1/来访问此微服务
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/ #向服务发现server注册，所以arch-discovery要先启动
```

然后在代码中给`BizMs1Application.java` 添加`@EnableDiscoveryClient`注解，让`biz-ms1`向eureka上注册服务

### 第一个控制器

New HelloController.java，并修改代码

``` java
@RestController
@RequestMapping("/")
public class HelloController {
    // 开启日志，为后面的负载均衡做准备
    private static Logger logger = LoggerFactory.getLogger(HelloController.class);
    
    // 加这个port，是为了验证后面的负载均衡
    @Value(value = "${server.port}")
    private String port;

    @RequestMapping("/")
    public String index() {
        // 后续，通过修改port值来启动不同的biz-ms1实例，通过查看log输来验证负载到哪个具体的实例
        logger.error("port:" + port);
        return "Greetings from Spring Boot!";
    }

    @RequestMapping("hello")
    public Map<String, Object> hello(@RequestParam(name = "name", defaultValue = "guest") String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "hello " + name);
        return result;
    }
}
```

### 注解说明

@RestController表示它是一个RESTFul风格的控制器，返回的Content-Type为application/json，所有的返回都由spring自动转为json格式，相当于@Controoler + @ResponseBody

@RequestMapping注释表示“路由”规则（访问路径及规则），在上面的代码中，我们定义了两个路由

1. `/` 根节点映射的是index方法，输出`Greetings from Spring Boot!`欢迎信息
2. `/hello` hello路径映射的是hello方法，并接受一个名为`name`的参数，返回的是一个map，（spring会转为一个json对象输出到客户端）

关于更多的信息，请参考[Spring framework MVC]。

### 运行

如果运行成功，则控制台会提示`Started BizMs1Application in xxx seconds (JVM running for xxx)`，并且通过浏览器可以访问 http://localhost:8080 会得到Greetings from Spring Boot!输出。

```

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v2.0.2.RELEASE)
....... . . .
....... . . . (log output here)
....... . . .
........ Started BizMs1Application in 2.222 seconds (JVM running for 6.514)

```

### 查看微服务信息

添加一个查看微服务实例的控制器，为方便，直接在`BizMs1Application.java`文件中添加一个`ServiceInstanceRestController`类

``` java
// client
@RestController
class ServiceInstanceRestController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping("/service-instances/{applicationName}")
    public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
        return this.discoveryClient.getInstances(applicationName);
    }
}
```

在这里定义了一个applicationName的路径变量（不可以为空），作为微服务的名称。在本章中，微服务的名称为biz-ms1，那么我们可以通过http://localhost:8080/service-instances/biz-ms1来查看相关的信息。


``` bash
$ curl http://localhost:8080/service-instances/biz-ms1
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  1133    0  1133    0     0  17984      0 --:--:-- --:--:-- --:--:-- 24106[{"host":"192.168.133.15","port":8080,"serviceId":"BIZ-MS1","metadata":{"management.port":"8080","jmx.port":"49765"},"uri":"http://192.168.133.15:8080","secure":false,"instanceInfo":{"instanceId":"192.168.133.15:8080","app":"BIZ-MS1","appGroupName":null,"ipAddr":"192.168.133.15","sid":"na","homePageUrl":"http://192.168.133.15:8080/","statusPageUrl":"http://192.168.133.15:8080/actuator/info","healthCheckUrl":"http://192.168.133.15:8080/actuator/health","secureHealthCheckUrl":null,"vipAddress":"biz-ms1","secureVipAddress":"biz-ms1","countryId":1,"dataCenterInfo":{"@class":"com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo","name":"MyOwn"},"hostName":"192.168.133.15","status":"UP","leaseInfo":{"renewalIntervalInSecs":30,"durationInSecs":90,"registrationTimestamp":1527388542940,"lastRenewalTimestamp":1527388542940,"evictionTimestamp":0,"serviceUpTimestamp":1527388542435},"isCoordinatingDiscoveryServer":false,"metadata":{"management.port":"8080","jmx.port":"49765"},"lastUpdatedTimestamp":1527388542940,"lastDirtyTimestamp":1527388542402,"actionType":"ADDED","asgName":null,"overriddenStatus":"UNKNOWN"},"scheme":null}]
```

为直观显示，建议使用浏览器访问（建议安装相应的json显示插件，或者通过F12查看网络中的响应信息）

### 测试验证

还是老规矩，使用测试用例来测试。

注：本节会使用多种测试方法，相关的测试请参考[TEST.MD](../TEST.MD)

选择HelloController.java -> New JUnit Test Case -> HelloControllerTest.java

本用例使用使用MockMvc + jsonPath来测试

```java

    @Autowired
    MockMvc mvc;
    
    @Test
    public void testIndex() throws Exception {
        // @formatter:off
        mvc.perform(MockMvcRequestBuilders.get("http://biz-ms1/")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo("Greetings from Spring Boot!")));
        // @formatter:on
    }

    @Test
    public void testHello() throws Exception {
        // @formatter:off
        mvc.perform(MockMvcRequestBuilders.get("/hello")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.msg").value("hello guest"));
        
        mvc.perform(MockMvcRequestBuilders.get("/hello?name=Jamling")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.msg").value("hello Jamling"));
        // @formatter:on
    }
```

然后再创建一个HelloControllerTest2.java，使用RestTemplate来测试

``` java
    @Autowired
    private TestRestTemplate template;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/").toString();
        // 如果是微服务消费方，可以使用微服务名字来调用
        // this.base = new URL("http://biz-ms1/").toString();
    }

    @Test
    public void testIndex() {
        // 使用 xxxForEntity
        ResponseEntity<String> response = template.getForEntity(base, String.class);
        assertThat(response.getBody(), equalTo("Greetings from Spring Boot!"));
    }

    @Test
    public void testHello() {
        // 使用RestTemplate.xxxForObject，将json转为java对象
        Info ret = template.getForObject(base + "hello", Info.class);
        assertEquals("hello guest", ret.msg);

        // 带url参数变量
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "Jamling");
        ret = template.getForObject(base + "hello?name={name}", Info.class, vars);
        assertEquals("hello Jamling", ret.msg);
    }

    static class Info {
        public int code;
        public String msg;
    }
```

最后测试一下微服务信息，直接使用Spring Boot Starter自动创建的BizMs1ApplicationTests来写测试用例代码

```java
    @Test
    public void contextLoads() {
        // 测试Eureka Client，如果biz-ms1微服务已启动，则测试通过
        List<ServiceInstance> instances = client.getInstances("biz-ms1");
        if (instances == null || instances.isEmpty()) {
            fail("Please confirm you started biz-ms1 and register to eureka server");
        } else {
            ServiceInstance instance = instances.get(0);
            System.out.println(instance);
            String uri = instance.getUri().toString();
            String host = instance.getHost();
            int port = instance.getPort();
            assertNotNull(uri);
            assertNotNull(host);
            assertEquals(8080, port);
        }

    }
```

注：此用例需要biz-ms1微服务先启动，不然会fail

### 参考

- [Spring Boot Reference]
- [Spring framework MVC]

[Spring Boot Reference]: https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/

[Spring framework MVC]: https://docs.spring.io/spring/docs/5.0.6.RELEASE/spring-framework-reference/web.html#mvc
