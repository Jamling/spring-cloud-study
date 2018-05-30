package com.example.arch.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public abstract class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    // 配置Bean，不然会报required a bean of type 'org.springframework.security.authentication.AuthenticationManager' that could
    // not be found
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .requestMatchers()
                .anyRequest() // 其余默认均可访问
                .and()
            .authorizeRequests()
                .antMatchers("/admin/**").hasRole("ADMIN") // admin/只有ADMIN角色才可以访问
                .antMatchers("/client/**").authenticated() // client/认证通过的用户访问
                .antMatchers("/oauth/**").permitAll() // 允许全部访问
                .and()
            .logout()
                .and()
            .formLogin() // 表单认证，默认是http basic
                //.loginPage("/login") // 自己定制登录界面
                //.permitAll()
                .and()
            .csrf().disable() // 禁用csrf，不然后续使用curl得到的token去请求受保护的资源会失败
            .httpBasic()
            ;
        // @formatter:on
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 忽略的资源列表，比如图片，公共css, js
        web.debug(true);
        web.ignoring().antMatchers("/favicon.ico");
    }
}
