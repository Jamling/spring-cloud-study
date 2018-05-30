## UPMS

UPMS (User Permission Management System) 几乎每个系统都有，本节只提供一下最基本的查询用户功能。

### 新建biz-upms工程

New Spring Boot Starter Project -> biz-upms

使用mybatis+mysql连接数据库，pom中添加`mysql-connector-java`和`mybatis-spring-boot-starter`

```xml
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.mybatis.spring.boot</groupId>
			<artifactId>mybatis-spring-boot-starter</artifactId>
			<version>1.3.2</version>
		</dependency>
```

PS：不知道mybatis-spring-boot-starter跟STS什么仇什么怨，一直无法在Select Dependences中搜索。

### 代码修改

application.properties => application.yml

```yml
server:
  port: 1000
spring:
  application:
    name: upms
  datasource:
    url: jdbc:mysql://localhost:3306/test?useUnicode=true&amp;characterEncoding=utf-8
    username: root
    password: root
    driver-class-name: com.mysql.jdbc.Driver
```

BizUpmsAppliction添加`@EnableDiscoveryClient`类注解

根据数据库设计，编写AccountInfo实体类

在dao包中新建AccountMapper接口，使用注解的方式（mybatis推荐的）

```java
@Mapper
public interface AccountMapper {
    @Select("select t.phone, t.uid, t.head, t.email from qw_member t where t.phone = #{phone} and t.password = #{password}")
    AccountInfo findByPhonePassword(@Param("phone") String phone, @Param("password") String password);
}
```

在service包中新建AccountService类，调用Mapper来执行查询

在controller包中新建AccountController类，提供`/account`路由，根据用户名和密码来查找用户。

### 对接arch-oauth

启动biz-upms，arch-oauth便可以通过UpmsService来调用biz-upms接口来实现真实的用户认证了。
