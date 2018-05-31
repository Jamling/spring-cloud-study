# spring-cloud-study

## 引言

关于Spring Cloud微服务的学习简明教程及实践，本教程与Spring官方等教程有所不同。因为本人没有系统地学习过Spring Boot及Spring Cloud，原因是简单：公司要求，而且非常紧急（只给四周的时间，含具体的业务微服务实现），所以代码偏多，教程文档偏少，要求读者具备一定的Spring Boot基础知识（或者足够聪明）。教程的顺序方面也是根据实际的业务，按过程实现的（但是在教程中，有部分教程前置了，比如网关，权限认证等，在实际实施过程中，是较后实现的）。

首先放上官方的Spring Cloud构架图

![Spring Cloud](https://spring.io/img/homepage/diagram-distributed-systems.svg)

Spring网站上有Getting Started Guides，本人一开始也是从这开始的，但是不好意思，第一篇config就看得吃力。再陆续看了后面的几节，比如Broker这些，感觉与快速实现公司的任务不是那么重要，所以跳过了，取而代之的是本人自己的一条学习路线。

![Spring Cloud Study Step](./Spring_Cloud_Study_Mind.png)

为啥要先从微服务注册开始呢？
因为微服务实施的关键在于服务治理，服务治理首先得需要一个注册中心吧，而几乎所有的微服务都需要向注册中心注册。所以第一个实践便是[arch-discovery]了。

然后再开发具体的业务微服务，并且向注册中心注册，通过网关向外部提供统一的访问接口。最后，以网关为中心，开发各种弱功能性业务。至此在本机上应该有一套完整的微服务架构系统，并且可以正常工作了。

但是我们的目的并不仅限于在本机运行，还要批量化生产及部署。开始需要考虑不同的环境与不同的配置了，比如测试环境，连接的是测试数据库，生产环境连接的是正式数据库，如此动态配置么？此时就引入了spring config作为统一的配置管理中心。

PS: 对于初学者，是一步一步从简到繁，最后实现一个完整的系统。但是对于系统架构人员，则需要提前规划，并决策采用什么样的具体技术。所以本教程的阅读对象为Spring Cloud新手，但是非常欢迎各个阶层的读者能提出宝贵意见及教程中的不当之处（以issue的方式提交），本人将对此教程持续更新与完善。

## 目录

- [arch-discovery](./arch-discovery/): 微服务注册与发现 
- [biz-ms1](./biz-ms1): 第一个具体的微服务，同时也作为服务生产者
- [biz-ms2](./biz-ms2): 第二个具体的微服务，同时也作为服务消费者
- [arch-gateway](./arch-gateway): Zuul网关，路由及转发，负载均衡，熔断
- [arch-oauth](./arch-oauth): OAuth2认证，含openfeign的使用
	- [biz-upms](./biz-upms): 使用mybatis+mysql验证用户名和密码
- [arch-config](./arch-config): 统一配置中心Config Server
	- [arch-config-test](./arch-config-client): 测试的Config Client
	- [arch-config-repo](./arch-config-repo): 配置git仓库
	
杂项

- Spring Actuator
- 打包
- Docker部署

## 特别说明

 - 本实践主要参考Spring官方文档与示例，部分改动来自百度搜索
 - 本系列要求读者具备一定的Spring Boot基础知识，对微服务有一定的了解
 - 所有的子项目均使用Spring Boot 2.0.X版本
 - 所有的子项目均为Spring Starter Project
 - 所有的子项目配置将为`application.yml`或`bootstrap.yml`，因为本人太爱`yaml`了，一般是新建完工程的第一件事就是将`application.properties`改为`application.yml`
 - 所有的子项目均带一个Spring Starter 链接，复制到浏览器中访问可生成一个`.zip`初始化工程

## 开发环境

- Windows 7
- JDK 1.8
- Maven 3.5
- Spring Tool Suite (3.9.4) 可在https://spring.io/tools 下载。

