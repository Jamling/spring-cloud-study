package com.example.arch.oauth.prd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.arch.oauth.config.SecurityConfiguration;

@Configuration
@Profile("prd")
public class MySecurityConfiguration extends SecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new MyPasswordEncoder();
    }

    @Autowired
    private MyAuthenticationProvider myAuthenticationProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(myAuthenticationProvider);
        super.configure(auth);
    }

    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }
}
