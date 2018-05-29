package com.example.arch.gateway;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.google.common.base.Predicates;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Configuration {

    @Bean
    public Docket docket() {
        // @formatter:off
        return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .select()
            .paths(Predicates.not(PathSelectors.ant("/actuator/**")))
            .paths(Predicates.not(PathSelectors.ant("/error/**")))
            //.apis(RequestHandlerSelectors.basePackage(ArchGatewayApplication.class.getPackage().getName()))
            .build()
            ;
        // @formatter:on
    }

    private ApiInfo apiInfo() {
        // @formatter:off
        return new ApiInfoBuilder()
            .title("XXX API")
            .description("XXX Description")
            .version("v1.0")
            .contact(new Contact("Jamling", "http://192.168.133.15", "li.jamling@gmail.com"))
            .build();
        // @formatter:on
    }

    // 开启分组api，因为实际上的业务非常复杂，所以按子系统单独出API
    @Configuration
    @Primary // instead inMemorySwaggerResourcesProvider
    public static class ResourceProvider implements SwaggerResourcesProvider {

        @Override
        public List<SwaggerResource> get() {
            List<SwaggerResource> list = new ArrayList<>();
            list.add(swaggerResource("全局API", "/v2/api-docs", "2.0"));
            // 需要在biz-ms1中集成swagger2
            list.add(swaggerResource("biz-ms1 API", "/biz-ms1/v2/api-docs", "2.0"));
            return list;
        }

        private SwaggerResource swaggerResource(String name, String location, String version) {
            SwaggerResource swaggerResource = new SwaggerResource();
            swaggerResource.setName(name);
            swaggerResource.setLocation(location);
            swaggerResource.setSwaggerVersion(version);
            return swaggerResource;
        }
    }
}
