## 第二个微服务

创建第二个微服务biz-ms2作为biz-ms1的消费者，即biz-ms2需要通过调用biz-ms1来执行一些操作。

### 创建工程

与biz-ms1类似，starter链接如下：

https://start.spring.io/starter.zip?name=biz-ms2&groupId=com.example&artifactId=biz-ms2&version=0.0.1-SNAPSHOT&description=Consumer&packageName=com.example.biz.ms2&type=maven-project&packaging=jar&javaVersion=1.8&language=java&bootVersion=2.0.1.RELEASE&dependencies=cloud-eureka&dependencies=cloud-ribbon&dependencies=web

### 配置

```yml
server:
  port: 8081

spring:
  application:
    name: biz-ms2
    
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### 修改代码

让消费者支持负载均衡。

1. 给BizMs2Application.java加上@EnableDiscoveryClient和@RibbonClient注解
  1.1 @EnableDiscoveryClient，声明其为一个Eureka客户端
  1.2 @RibbonClient，声明其为一个使用Ribbon作为负载均衡的客户端
  
2. 给BizMs2Application.java注入一个支持负载均衡的RestTemplate

```
    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
```

注：更多介绍请参考http://cloud.spring.io/spring-cloud-static/Edgware.SR3/single/spring-cloud.html#_customizing_the_ribbon_client (部分示例在`bootstrap.yml`和`RibbonConfiguration.java`中有体现)


New ConsumerController，添加index方法，路由为`/`，直接使用RestTemplate去请求biz-ms1的index方法（路由为`/`）

```java
    @Autowired
    RestTemplate template;

    @RequestMapping(path = "/")
    public String index() {
        String welcome = template.getForObject("http://biz-ms1", String.class);
        log.error("welcome: " + welcome);
        return welcome;
    }
```

### 运行多个生产者

通过STS中的Boot Dashboard面板，复制biz-ms1的运行配置（Duplicate Config），并修改端口号为7079（Arguments选项卡，在VM arguments中添加 -Dserver.port=8079）并运行
此时，除了原有的biz-ms1 (8080)，再加上刚才的biz-ms1(7079)，我们已经运行了两个biz-ms1了。加上biz-ms1预先埋点的负载均衡的验证日志，可以测试biz-ms2的负载均衡了

### 运行消费者

运行biz-ms2，在浏览器中打开 http://localhost:8081/ 并多请求几次，观察STS console中biz-ms1(8080)和biz-ms1(7090)的日志。如果biz-ms1的实例都执行了（会打印各自的端口号），则表明ribbon负载均衡工作正常。（ribbon默认使用轮询规则，可以通过yml配置或代码修改，biz-ms2中均有示例）

### 测试

src/test/java下有一个ConsumerControllerTest.java测试用例，只包含基本的调用测试，如果想测试负载均衡，可对biz-ms1进行改造，添加一个port方法，返回biz-ms1的端口号，然后在本测试用例中判断端口号是否在期望的端口号列表中。

PS：此用例需提前启动biz-ms1才能执行成功



