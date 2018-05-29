package com.example.arch.gateway;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/")
@Api(tags = "刷新配置", description = "当配置修改后，通过git hook自动修改所有微服务的配置")
public class GatewayController {
    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private RestTemplate template;

    @GetMapping("/")
    public Object index() {
        return template.getForObject("http://biz-ms1", String.class);
    }

    @ApiOperation(value = "更新配置", notes = "更新所有微服务（不包含网关自己）的配置")
    @GetMapping("refresh")
    public Object refresh() {
        List<Route> routes = routeLocator.getRoutes();
        Map<String, Integer> result = new LinkedHashMap<>();
        if (routes != null) {
            for (int i = 0; i < routes.size(); i++) {
                Route route = routes.get(i);
                if (!StringUtils.isEmpty(route.getLocation()) && !route.getLocation().contains("://")) {
                    String url = String.format("http://%s/actuator/refresh", route.getLocation());
                    ResponseEntity<String> response = template.postForEntity(url, null, String.class);
                    result.put(route.getLocation(), response.getStatusCodeValue());
                }
            }
        }
        return result;
    }
}
