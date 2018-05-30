package com.example.arch.oauth.prd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.arch.oauth.entity.AccountInfo;
import com.example.arch.oauth.service.UpmsService;

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
