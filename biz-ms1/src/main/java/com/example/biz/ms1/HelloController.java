package com.example.biz.ms1;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    
//    @RequestMapping("port")
//    public int port() {
//        return port();
//    }
}
