package com.example.arch.oauth.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import com.example.arch.oauth.config.SecurityConfiguration;

@Configuration
@Profile("test")
public class TestSecurityConfiguration extends SecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // @formatter:off
        // 在此添加的用户会报找不到用户名异常，TODO 为啥呢？
//        auth.inMemoryAuthentication().withUser("user")
//            .password("{noop}123456")
//            .roles("USER")
//            .and().withUser("admin")
//            .password("{bcrypt}$2a$10$Su.zsYowieJiq53blfvEHOISr0tNTNDhG.XYNBntCqnnPDC9XaxNq")
//            .roles("USER", "ADMIN")
//            ;
        // @formatter:on
        super.configure(auth);
    }

    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        // @formatter:off
        UserDetails user1 = User.withUsername("user")
                   .password("{noop}123456")
                   //.password("123456")
                   .roles("USER")
                   .build();
        UserDetails user2 = User.withUsername("admin")
                   .password("{bcrypt}$2a$10$Su.zsYowieJiq53blfvEHOISr0tNTNDhG.XYNBntCqnnPDC9XaxNq")
                   .roles("ADMIN")
                   .build();
        // @formatter:on
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(user1);
        manager.createUser(user2);
        return manager;
    }
}
