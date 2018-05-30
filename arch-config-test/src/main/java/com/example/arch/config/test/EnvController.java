package com.example.arch.config.test;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
