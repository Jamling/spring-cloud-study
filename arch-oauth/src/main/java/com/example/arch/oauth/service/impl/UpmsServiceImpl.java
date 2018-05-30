package com.example.arch.oauth.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.arch.oauth.entity.AccountInfo;
import com.example.arch.oauth.service.UpmsService;

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
