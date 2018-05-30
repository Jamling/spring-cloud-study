package com.example.arch.oauth.service.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.example.arch.oauth.entity.AccountInfo;
import com.example.arch.oauth.service.UpmsService;

@Service
@Profile("prd")
public class UpmsServiceFallback implements UpmsService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public AccountInfo findUserByUsernamePassword(String username, String password) {
        logger.error("调用{}异常:{}{}", "findUserByUsernamePassword", username, password);
        AccountInfo info = new AccountInfo();
        // 模拟返回一个用户
        info.phone = username;
        // 123456加密后的密码
        // info.password = "5f1d7a84db00d2fce00b31a7fc73224f";
        info.password = password;
        // return info;
        return null;
    }
}
