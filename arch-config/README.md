## 配置中心

- 配置中心，比如使用git/svn，将配置文件按规则组织好
- 配置服务器，从配置中心不停地拉取配置并解析配置
- 配置客户端，启动时从配置服务器获取配置，与本地配置合并

### 配置中心

新建普通项目[arch-config-repo](../arch-config-repo)，用来放配置文件

全局配置，名为application.yml，所有客户端都共享的配置

客户端全局配置，名为${application}.yml，所有名为${application}的客户端都共享的配置

客户端指定环境配置，名为${application}-${profile}.yml

PS: 这里不讨论${label}（分支，默认为master）。

PS: 配置可以继承（共享）和重写。比如application.yml中定义了global.foo=bar，那么所有的客户端配置都含有global.foo环境变量（这其实是配置服务器做了合并），客户端也可以重新指定global.foo的值（重写）。

### 配置服务端

新建Spring Boot Starter Project -> arch-config

Project Dependence选择Cloud -> Config

https://start.spring.io/starter.zip?name=arch-config&groupId=com.example&artifactId=arch-config&version=0.0.1-SNAPSHOT&description=Config+Server+Module&packageName=com.example.arch.config&type=maven-project&packaging=jar&javaVersion=1.8&language=java&bootVersion=2.0.2.RELEASE&dependencies=cloud-config-server

或者自己手动添加`spring-cloud-config-server`依赖

ArchConfigApplication.java中添加`@EnableConfigServer`类注解

application.properties -> application.yml

```yml
server:
  port: 8888 #建议默认端口号，这样config client可以不用配置cloud config
spring:
  cloud:
    config:
      server: # 服务端配置
        git:
          uri: file://${user.home}/config-repo # 测试环境使用本地地址，免commit直接生效
---
spring:
  profiles: github # 此环境使用教程中的github仓库做为配置中心
  cloud:
    config:
      server:
        git:
          uri: https://github.com/Jamling/spring-cloud-study
          search-paths:
          - arch-config-repo
```

配置中心服务器为我们提供了一些Endpoint来查看配置

- /${application}/${profile}: 查看spring.application.name为${application}，spring.profiles.active为${profile}的配置文件
如curl http://localhost:8888/abc/dev

```json
{
  "name": "abc",
  "profiles": [
    "dev"
  ],
  "label": null,
  "version": "9f82927d100284f31660b8ab784d6bc01cd02785",
  "state": null,
  "propertySources": [
    {
      "name": "file:///d:/work/workspace-sts/spring-cloud-study/arch-config-repo/application.yml",
      "source": {
        "global.foo": "bar"
      }
    }
  ]
}
```

- /${application}-${profile}.yml 查看spring.application.name为${application}，spring.profiles.active为${profile}的配置内容
如curl http://localhost:8888/client-dev.yml

```json
global:
  foo: bar
name: client-dev
server:
  application:
    foo: server application foo bar
  bootstrap:
    foo: bar
```

PS: 还有好多访问节点，请参考[Spring Cloud Config]或查看config server的启动日志。可以启动config server，自己访问实践一下。

### 配置客户端

New Spring Boot Starter Project -> arch-config-test

https://start.spring.io/starter.zip?name=arch-config-test&groupId=com.example&artifactId=arch-config-test&version=0.0.1-SNAPSHOT&description=Config+Server+Client+Module&packageName=com.example.arch.config.test&type=maven-project&packaging=jar&javaVersion=1.8&language=java&bootVersion=2.0.2.RELEASE&dependencies=cloud-config-client&dependencies=cloud-eureka&dependencies=web

application.properties -> application.yml

```yml
spring:
  application:
    name: client
  cloud:
    config:
      uri: http://localhost:8888
#      discovery: #使用服务发现，如不想写死上面的uri，开启此方式
#        enabled: true
#        service-id: config-server 
      
management:
  endpoints:
    web:
      exposure:
        include: "*"
        exclude:
        - env
      
client:
  application:
    foo: ${server.application.foo:} # 使用服务端的server.application.foo，如果找不到，默认为空
```

spring.application.name为client，对应的配置文件为配置中心client-*.yml。

PS: 先启动config server，不然有些配置项会使用默认值。

编写EvnController来测试配置项

```java
@RestController
public class EnvController implements ApplicationContextAware {

    @RequestMapping("/env")
    public Object env() {
        Map<String, Object> env = new LinkedHashMap<String, Object>();
        org.springframework.core.env.Environment e = applicationContext.getEnvironment();
        // @formatter:off
        String[] keys = {
            "spring.application.name"
            , "spring.profiles.active"
            , "server.application.foo"
            , "server.bootstrap.foo"
            , "client.application.foo"
            , "client.bootstrap.foo"
            , "name"
            };
        // @formatter:on
        for (String key : keys) {
            env.put(key, e.getProperty(key));
        }
        return env;
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
```

config server启动时

curl http://localhost:8080/env

```json
{
  "spring.application.name": "client",
  "spring.profiles.active": null,
  "server.application.foo": "server application foo bar",
  "server.bootstrap.foo": "bar",
  "client.application.foo": "server application foo bar",
  "client.bootstrap.foo": "bar",
  "name": "client"
}
```

未启动时，则是

```json
{
  "spring.application.name": "client",
  "spring.profiles.active": null,
  "server.application.foo": null,
  "server.bootstrap.foo": null,
  "client.application.foo": "",
  "client.bootstrap.foo": "",
  "name": null
}
```

PS: Environment不能获取数组值，比如spring.profiles.active是一个数组，`Environment.getProperty("spring.profiles.active")`会返回null，需要通过key[index]来获取。index为数组下标值


### 配置热更新

如果配置修改了，可以自动或手动通知客户端来更新配置。

此时，又需要借助`spring-boot-starter-actuator`这个Spring神器了。记得先在yml配置中暴露refresh Endpoint

```yml
management:
  endpoints:
    web:
      exposure:
        include: "*"
        exclude:
        - env
```

然后手动
`POST http://localhost:8080/actuator/refresh`，如果成功则可以不重启client来实现热更新

PS: 自动刷新须借助带有web hook的scm系统，比如github就支持web hook的。

### 参考

- [Centralized Configuration]
- [Spring Cloud Config]

[Centralized Configuration]: https://spring.io/guides/gs/centralized-configuration/
[Spring Cloud Config]: http://cloud.spring.io/spring-cloud-static/Edgware.SR3/single/spring-cloud.html#_spring_cloud_config