package com.example.arch.oauth.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.arch.oauth.entity.AccountInfo;
import com.example.arch.oauth.service.fallback.UpmsServiceFallback;

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
