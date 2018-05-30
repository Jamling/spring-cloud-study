## OAuth2认证

在学习本章之前，需要先了解以下两个基础知识

- OAuth2: 可以参考[OAuth2理解 － 阮一峰]
- Spring Security: 可以参考[参考](#参考)一节

个人简要理解，OAuth就是不同的软件体系之间交换用户凭证与资源的一种标准协议。每个系统（假设为A）一般都有自己的用户体系和受保护的资源，如果想访问别的系统（假设为B）资源，需要向其发送一个请求，请求通过后返回A一个临时token，有了这个token，A便可以获取B上的资源了。

一般来说，每个系统都有一套自己的认证体系（未必是OAuth），如果涉及系统交互，那么必然有不同的规范与接口，那么如何解决这个问题呢？答案便是统一，大家都按相同的规范来，所以OAuth就应运而生了。

个人觉得，这里面跟业务相关最强的莫过于认证了，系统A向系统B请求访问，需要B对A认证吧，系统B也需要对用户做认证吧？比如在自己的系统中使用QQ登录，QQ需要对你的系统认证（client认证），同时也需要对用户请求认证（user验证：QQ号及其密码是否匹配）

所以呢，在此节，分两个部分

- 系统内用户认证，几乎每个系统都需要
- 系统外的客户端认证，以前的单体构架系统不重要这个，但微服务就有必要搞这些了，还有，万一你的系统很NB，具有像QQ一样庞大的用户群和日活，别的系统都想请求你的系统资源呢。
- 其实还有第三部分，使用第三方的账号体系登录，略（有时间再出一个子章节）。

### 创建arch-oauth项目

New Spring Boot Stater Project -> arch-oauth

https://start.spring.io/starter.zip?name=arch-oauth&groupId=com.example&artifactId=arch-oauth&version=0.0.1-SNAPSHOT&description=OAuth2+server&packageName=com.example.arch.oauth&type=maven-project&packaging=jar&javaVersion=1.8&language=java&bootVersion=2.0.1.RELEASE&dependencies=cloud-oauth2&dependencies=cloud-security&dependencies=thymeleaf&dependencies=web

### 配置

application.properties => application.yml

```yml
server:
  port: 1111
  
spring:
  application:
    name: oauth
  profiles:
    active: # 当前profile为test, TestSeucrityConfiguration有效
    - test

logging:
  level:
    org.springframework.security: DEBUG #初学者建议打开debug日志

--- 
spring:
  profiles: prd # 这里只是示例怎么使用多环境(profile)配置，在当前配置下MySeucrityConfiguration有效
  
```

在这一章，引入了一个新的配置项: profiles。在arch-oauth中定义了两个profile

1. test: 测试的，像用户认证使用内存中预定义的用户进行认证
2. prd: 正式的，对接的是真实的业务系统，需调用biz-upms从数据库对用户进行验证（源代码中的prd, entity, service三个子包都是跟正式环境有关）

### 代码修改

打开`ArchOauthApplication.java`在类上添加@RestController注解，并添加一些测试方法，设置以下路由

- `/`: 所有用户（含未登录的匿名用户）都可以访问，输出Hello xxxuser
- `/client`: 登录用户可访问
- `/client`: 登录用户并且admin角色可访问
- `/me`: 登录用户可访问，并且纳入资源服务器保护的资源列表
- `user`: 输出结果同`/me`，不过未纳入资源服务器保护的资源列表

新建`SecurityConfiguration`抽象类并继承 `WebSecurityConfigurerAdapter`，类注解中添加`@EnableWebSecurity`开启Spring Security。

重写`authenticationManagerBean`方法，并添加`@Bean`注解，不然会报`required a bean of type 'org.springframework.security.authentication.AuthenticationManager' that could not be found`异常，导致启动不成功。

重写`configure`访问并配置访问规则

```java
@EnableWebSecurity
public abstract class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    // 配置Bean，不然会报required a bean of type 'org.springframework.security.authentication.AuthenticationManager' that could
    // not be found
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .requestMatchers()
                .anyRequest() // 其余默认均可访问
                .and()
            .authorizeRequests()
                .antMatchers("/admin/**").hasRole("ADMIN") // admin/只有ADMIN角色才可以访问
                .antMatchers("/client/**").authenticated() // client/认证通过的用户访问
                .antMatchers("/oauth/**").permitAll() // 允许全部访问
                .and()
            .logout()
                .and()
            .formLogin() // 表单认证，默认是http basic
                //.loginPage("/login") // 自己定制登录界面
                //.permitAll()
                .and()
            .csrf().disable() // 禁用csrf，不然后续使用curl得到的token去请求受保护的资源会失败
            .httpBasic()
            ;
        // @formatter:on
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 忽略的资源列表，比如图片，公共css, js
        web.debug(true);
        web.ignoring().antMatchers("/favicon.ico");
    }
}
```

注：此类为抽象类，具体的认证分别在正式环境和测试环境下有具体的实现

### 认证

在开始具体的认证之前，先讲几个相关的重要的spring类

- Authentication: 封装了请求信息中的用户名，密码或token等信息，它会提交给AuthenticationManager去做认证(authenticate)。比如常见的`UsernamePasswordAuthenticationToken`和`OAuth2Authentication`都是它的具体实现类

- AuthenticationManager: Spring定义的认证接口，所有的认证实现类必须实现此接口，它只有一个抽象方法：`Authentication authenticate(Authentication authentication) throws AuthenticationException;`，如果一个Authentication是受信任的（密码正确，未过期，未锁定等），那么设置其为已认证（authenticated）

- ProviderManager: AuthenticationManager的实现类，它是一棵ProviderManager树，包含一个List<AuthenticationProvider>，由具体的AuthenticationProvider来authenticate，如果都失败则由parent ProviderManager继续authenticate。
PS：实际上发现最上层的parent好像就是它自己，所以如果子ProviderManager认证不成功，则会导致StackOverFlow。

- UserDetailsService: 根据Authentication来查找用户实体信息(UserDetails)

- UserDetails: 用户实体信息，不过密码被擦除掉了

PS: Authentication, authenticate, 这些英文，我也不知道如何准确地翻译。建议参考Spring相关的文档自行体会。

下面分测试环境和正式环境来实现具体的认证

### 测试环境

新建`TestSecurityConfiguration.java`继承自`SecurityConfiguration.java` ，在内存中创建两个用户user和admin，密码都为123456，其中admin拥有ADMIN角色。核心代码如下

```java
    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        // @formatter:off
        UserDetails user1 = User.withUsername("user")
                   .password("{noop}123456")
                   //.password("123456")
                   .roles("USER")
                   .build();
        UserDetails user2 = User.withUsername("admin")
                   .password("{bcrypt}$2a$10$Su.zsYowieJiq53blfvEHOISr0tNTNDhG.XYNBntCqnnPDC9XaxNq")
                   .roles("ADMIN")
                   .build();
        // @formatter:on
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(user1);
        manager.createUser(user2);
        return manager;
    }
```

在这里引入了`PasswordEncoder`，Spring自己封装了一些常用的加密方式，默认使用bcrypt加密方式。

注意：测试环境下需要设置`spring.profiles.active=test`才能成功运行。运行成功后使用浏览器访问`http://localhost:1111/client`会跳到登录界面，输入user/123456后，可以成功查看当前用户的认证信息

```json
{
	"password": null,
	"username": "user",
	"authorities": [{
		"authority": "ROLE_USER"
	}],
	"accountNonExpired": true,
	"accountNonLocked": true,
	"credentialsNonExpired": true,
	"enabled": true
}
```

### 正式环境

正式环境中，所有`@Configuration`和`@Component`都必须联合`Profile("prd")`使用，表示这些配置仅应用在`prd`环境中，因实际的业务的用户模型及加密规则都与Spring默认的不一样，所以需要根据业务实现自定义的认证规则

#### 用户模型

因数据库中的用户实体模型AccountInfo与Spring的UserDetails（User）属性不一样，使用适配或代理模式新建`UserDetailsImpl.java`并实现`UserDetails`接口。关键代码如下

```java
private AccountInfo user;

    public UserDetailsImpl(AccountInfo user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorityList = new ArrayList<>();
        // for (SysRole role : user.get) {
        // authorityList.add(new SimpleGrantedAuthority(role.getRoleCode()));
        // }
        authorityList.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorityList;
    }

    @Override
    public String getPassword() {
        return "{my}" + user.password;
    }

    @Override
    public String getUsername() {
        return user.phone;
    }
```

#### 用户Service

类似，创建UserDetailsServiceImpl类并实现UserDetailsService接口来实现用户的查找

```java
/**
 * 真实的UserDetailService，调用upms微服务，根据用户名和密码查找用户
 */
@Service
@Profile("prd")
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UpmsService upmsService;

    // 这个方法是不用的，实际使用的是下面那个方法
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountInfo user = upmsService.findUserByUsernamePassword(username, null);
        return new UserDetailsImpl(user);
    }

    /**
     * 根据用户名和密码查找用户
     */
    public UserDetails loadUserByUsernamePassword(String username, String password) {
        AccountInfo user = upmsService.findUserByUsernamePassword(username, password);
        return new UserDetailsImpl(user);
    }
}
```

本Serive依赖UpmsService，暂留个坑，在Feign中再填

#### PasswordEncoder

实际业务系统中使用的加密规则为md5(md5(password)+password)，Spring内置的PasswordEncoder不支持此规则，需要自定义一个。关键代码如下

```java

    @Override
    public String encode(CharSequence rawPassword) {
        // 加密规则为md5(md5(password)+password)
        String str = rawPassword.toString();
        str = md5(str);
        str = str + rawPassword;
        str = md5(str);
        return str;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null && encodedPassword == null) {
            return true;
        }
        String id = extractId(encodedPassword);
        if (id == null) {
            // 顺带修复一下Spring明文密码校验
            return delegating.matches(rawPassword, "{noop}" + encodedPassword);
        } else if ("my".equals(id)) {
            return encode(rawPassword).equals(extractEncodedPassword(encodedPassword));
        }
        return delegating.matches(rawPassword, encodedPassword);
    }
```

实际业务系统中的加密名称定为my，如果不是my，则使用Spring内置规则去匹配

#### AuthenticationProvider

自定义一个名为MyAuthenticationProvider的AuthenticationProvider，关键代码如下：

```java
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName().trim();
        String password = ((String)authentication.getCredentials()).trim();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new BadCredentialsException("Login failed! Please try again.");
        }

        UserDetails user;
        try {
            user = userService.loadUserByUsernamePassword(username, encoder.encode(password));
        } catch (Exception e) {
            throw new BadCredentialsException("Please enter a valid username and password.");
        }

        // 不需要了
//        if (!encoder.matches(password, user.getPassword().trim())) {
//            throw new BadCredentialsException("Please enter a valid username and password.");
//        }

        if (!user.isEnabled()) {
            throw new DisabledException("Please enter a valid username and password.");
        }

        if (!user.isAccountNonLocked()) {
            throw new LockedException("Account locked. ");
        }

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<GrantedAuthority> permlist = new ArrayList<GrantedAuthority>(authorities);

        return new UsernamePasswordAuthenticationToken(user, password, permlist);
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
```

仅支持用户名+密码方式认证，委托上一节中的UserDetailsServiceImpl根据用户名和加密后的密码来查找用户。

#### SecurityConfiguration

最后放上MySecurityConfiguration的配置

```java
@Configuration
@Profile("prd")
public class MySecurityConfiguration extends SecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new MyPasswordEncoder();
    }

    @Autowired
    private MyAuthenticationProvider myAuthenticationProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(myAuthenticationProvider);
        super.configure(auth);
    }

    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }
}
```

在配置中，注入了上面的MyPasswordEncoder，手动设置了具体的MyAuthenticationProvider。

### Feign客户端

实际上，OAuth微服务并不直接连接数据库查询用户。用户的信息都由biz-upms微服务提供。所以通过Feign来访问upms服务。Feign相当于Ribbon+Hystrix。

要使用Feign，需要在pom中添加`spring-cloud-starter-openfeign`依赖，并在Application中添加`@EnableFeignClients`注解

还记得上面留的UmpsService坑么？下面填一下

```java
@FeignClient(name = "upms", fallback = UpmsServiceFallback.class)
public interface UpmsService {
    @GetMapping(value = "account")
    AccountInfo findUserByUsernamePassword(
    // @formatter:off
        @RequestParam("username") String username,
        @RequestParam("password") String password
        );
    // @formatter:on
}
```

添加了一个`@FeignClient`注解，指定微服务名称为umps，服务降级实现类为`UpmsServiceFallback`。

分别放上两个UpmsServer的实现类

impl/UpmsServiceImpl.java

```java
@Service
@Configuration
@Profile("prd")
public class UpmsServiceImpl implements UpmsService {

    public AccountInfo findUserByUsernamePassword(String username, String password) {
        AccountInfo user = null;
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        user = restTemplate().postForObject("http://upms/account", params, AccountInfo.class);
        return user;
    }

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

很简单，通过RestTemplate调用upms微服务的account接口

fallback/UpmsServiceFallback.java

```java
@Service
@Profile("prd")
public class UpmsServiceFallback implements UpmsService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public AccountInfo findUserByUsernamePassword(String username, String password) {
        logger.error("调用{}异常:{}{}", "findUserByUsernamePassword", username, password);
        AccountInfo info = new AccountInfo();
        // 模拟返回一个用户
        info.phone = "user";
        // 123456加密后的密码
        info.password = "5f1d7a84db00d2fce00b31a7fc73224f";
        return info;
        // return null;
    }
}
```

为提前测试，返回一个模拟的user用户。

### [biz-upms]

[biz-upms]中使用mybatis连接数据库并查询用户信息

### 认证服务器

在pom中添加`spring-cloud-starter-oauth2`可以让自己的系统秒变认证服务器。
新建AuthorizationServerConfiguration.java，添加`@EnableAuthorizationServer`类注解，然后提供一个AuthenticationManager来认证，最后提供几个模拟的内存客户端。关键代码如下

```java
    @Autowired
    AuthenticationManager authenticationManager;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // @formatter:off
        clients.inMemory()
            .withClient("client1")
                .authorities("client", "USER")
                .authorizedGrantTypes(IAuthConstants.AUTH_PASSWORD, IAuthConstants.AUTH_CODE, IAuthConstants.AUTH_CLIENT, IAuthConstants.AUTH_IMPLICIT)
                .secret("{noop}123456")
                .scopes("read")
                .redirectUris("http://localhost:1111/callback") // authorization_code授权模式下有效
                .autoApprove(true)
                .and()
            .withClient("client2")
                .authorities("client", "ADMIN")
                .authorizedGrantTypes(IAuthConstants.AUTH_CLIENT)
                .secret("{my}5f1d7a84db00d2fce00b31a7fc73224f") // password为123456
                .scopes("write")
                .autoApprove(true)
                ;
        // @formatter:on

    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // @formatter:off
        endpoints
            .authenticationManager(authenticationManager)
            // 允许GET，不然{"error":"method_not_allowed","error_description":"Request method &#39;GET&#39; not supported"}
            .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST)
            ;
        // @formatter:on
    }
```

上面的代码中
第一个`configure`方法在内存中创建了两个client，密码都为123456，不过支持不同的授权方式（grant_type）及权限（不想过多纠结，把client当作普通用户就行，只不过密码叫secret。相信有不少人集成过第三方的工具，一定熟悉appId和appSecret，可以想想其中有啥联系）。
第二个`configure`方法指定了oauth相关的节点由AuthenticationManager来进行认证。

#### 授权方式

再次提醒，先读一下[OAuth2理解 － 阮一峰]这篇博客。然后分别实践4种授权方式

##### 授权码方式
必须使用client1（因为只有它支持authorization_code方式的授权）这里特别说明一下，代码中已经配置了redirect_uri，所以请求参数中可以不用带。
`org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint#authorize()`方法会处理用户请求，如果用户未登录，需要登录。登录成功后，认证服务器会生成一个code并跳转到客户端redirect_uri，然后客户再去请求认证服务器去换token。
1. 浏览器访问http://localhost:1111/oauth/authorize?response_type=code&client_id=client1&state=random去拿code，（认证服务器对用户认证。可选，统一认证中，此过程不需要）成功后HTTP 302重定向到redirect_uri

``` json
{
    "user authentication":{
        "enabled":true,
        "password":"{my}null",
        "username":"user",
        "accountNonLocked":true,
        "authorities":[
            {
                "authority":"ROLE_USER"
            }
        ],
        "accountNonExpired":true,
        "credentialsNonExpired":true
    },
    "get token url":"http://localhost:1111/oauth/token?grant_type=authorization_code&code=NnCYdq&redirect_uri=http://localhost:1111/callback"

}
```

2. redirect_uri接收认证服务器传过来的code和state，并带上这些信息再次向认证服务器去获取token（必须用POST方式）。

```json
{
  "access_token": "d96a6786-5973-45ea-8ad4-d7cb1e0840ca",
  "token_type": "bearer",
  "expires_in": 43196,
  "scope": "read"
}
```

##### 简化模式

简化模式（implicit grant type）不通过第三方应用程序的服务器，直接在浏览器中向认证服务器申请令牌，跳过了"授权码"这个步骤

http://localhost:1111/oauth/authorize?response_type=token&client_id=client1&state=random&redirect_uri=http://localhost:1111/callback2

输入用户名和密码后认证成功后，HTTP 302重定向到http://localhost:1111/callback2#access_token=2d435b35-b418-42f6-a954-355be360dea9&token_type=bearer&state=random&expires_in=43199&scope=read

其中可以从地址栏中取出access_token (2d435b35-b418-42f6-a954-355be360dea9)

##### 客户端模式

这种模式在实际的业务系统中使用较多，比如微服务A需要调用微服务B，就需通过网关认证，拿到access_token，然后就可以恃无忌惮地访问任何微服务了（当然是在权限允许范围之内）。

`$ curl client1:123456@localhost:1111/oauth/token -d grant_type=client_credentials`
返回
`{"access_token":"4dc595fa-d429-4236-bb0f-cd516a144808","token_type":"bearer","expires_in":43199,"scope":"read"}`

##### 密码模式

这种模式下客户端需要携带用户的密码向认证服务器申请token（不得保存用户密码，但这个怎么保证呢？）。

`$ curl client1:123456@localhost:1111/oauth/token -d grant_type=password -d username=user -d password=123456`

 `{"access_token":"2752f32b-d249-4882-8ae8-21704f32126a","token_type":"bearer","expires_in":43199,"scope":"read"}`

### 资源服务器

使用`@EnableResourceServer`类注解可以开启资源服务器

```java
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.antMatcher("/me").authorizeRequests().anyRequest().authenticated();
        // @formatter:on
    }
}
```

经过`/me`的路由都会由`OAuth2AuthenticationProcessingFilter`拦截，如果OAuth2凭证（Authentication）认证不通过，将返回HTTP 401

```json
{
  "error": "unauthorized",
  "error_description": "Full authentication is required to access this resource"
}
```

可以使用*认证服务器*一节中任意一种方式获取的access_token来访问资源服务器受保护的`/me`资源，如使用客户端模式。在浏览器中带上curl中返回的access_token访问。

```json
{
    "authorities": [
        {
            "authority": "client"
        },
        {
            "authority": "USER"
        }
    ],
    "details": {
        "remoteAddress": "0:0:0:0:0:0:0:1",
        "sessionId": null,
        "tokenValue": "4dc595fa-d429-4236-bb0f-cd516a144808",
        "tokenType": "Bearer",
        "decodedDetails": null
    },
    "authenticated": true,
    "userAuthentication": null,
    "principal": "client1",
    "oauth2Request": {
        "clientId": "client1",
        "scope": [
            "read"
        ],
        "requestParameters": {
            "grant_type": "client_credentials"
        },
        "resourceIds": [
            
        ],
        "authorities": [
            {
                "authority": "client"
            },
            {
                "authority": "USER"
            }
        ],
        "approved": true,
        "refresh": false,
        "redirectUri": null,
        "responseTypes": [
            
        ],
        "extensions": {
            
        },
        "grantType": "client_credentials",
        "refreshTokenRequest": null
    },
    "credentials": "",
    "clientOnly": true,
    "name": "client1"
}
```

### 参考

[OAuth2理解 － 阮一峰]: http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html
[Securing a Web Application]: https://spring.io/guides/gs/securing-web/
[Spring Security Architecture]: https://spring.io/guides/topicals/spring-security-architecture/
[OAuth 2 Developers Guide]: https://projects.spring.io/spring-security-oauth/docs/oauth2.html
[Spring Security Reference]: https://docs.spring.io/spring-security/site/docs/5.0.2.RELEASE/reference/htmlsingle/#what-is-acegi-security
[Spring Boot and OAuth2]: https://spring.io/guides/tutorials/spring-boot-oauth2/
[biz-upms]: ../biz-upms
