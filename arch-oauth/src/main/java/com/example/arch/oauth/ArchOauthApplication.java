package com.example.arch.oauth;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableFeignClients
public class ArchOauthApplication {

    private Object getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                // return ((UserDetails)principal).getUsername();
            }
            return principal;
        }
        return "null authentication";
    }

    /**
     * 根目录，都可以访问
     */
    @RequestMapping("/")
    public String index() {
        return "Hello " + getUser();
    }

    /**
     * /oauth 路径，都可以访问
     */
    @RequestMapping("/oauth")
    public String oauth() {
        return "Hello OAuth2";
    }

    /**
     * 只有admin角色才可以访问
     */
    @RequestMapping("/admin")
    public String admin() {
        return "Hello Admin";
    }

    /**
     * 需授权用户（已登录）访问
     */
    @RequestMapping("/client")
    public Object client() {
        return getUser();
    }

    /**
     * 参考 {@link ResourceServerConfiguration}，/me被资源服务器定义为需授权才能访问
     */
    @RequestMapping({"/user", "/me"})
    public Principal user(Principal principal) {
        return principal;
    }

    /**
     * grant_type为authorization_code的回调，请忽略（本人亦未完全厘清具体的代码实现）
     */
    @RequestMapping("/callback")
    public Object authorization_code_callback(@RequestParam(required = true) String code,
        @RequestParam(required = false) String state) {
        String url = String.format("http://localhost:1111/oauth/token?grant_type=authorization_code&code=%s&redirect_uri=%s", code,
            "http://localhost:1111/callback");
        Map<String, Object> ret = new HashMap<>();
        ret.put("get token url", url);
        ret.put("user authentication", getUser());
        return ret;
    }
    
    @RequestMapping("/callback2")
    public Object implict_callback(Map<String, Object> parameters) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("implict callback", parameters);
        ret.put("user authentication", getUser());
        return ret;
    }

    public static void main(String[] args) {
        SpringApplication.run(ArchOauthApplication.class, args);
    }
}
