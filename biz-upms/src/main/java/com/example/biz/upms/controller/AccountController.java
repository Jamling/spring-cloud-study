package com.example.biz.upms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.biz.upms.entity.AccountInfo;
import com.example.biz.upms.service.AccountService;

@RestController
@RequestMapping("/")
public class AccountController {

    @Autowired
    AccountService accountService;

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public AccountInfo findByName(String username, String password) {
        AccountInfo info = accountService.loginByPhone(username, password);
        System.out.println(info);
        return info;
    }

}
