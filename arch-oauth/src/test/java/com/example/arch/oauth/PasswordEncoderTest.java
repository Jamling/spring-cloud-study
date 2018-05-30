package com.example.arch.oauth;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class PasswordEncoderTest {

    @Before
    public void setUp() throws Exception {}

    @Test
    public void test() {
        UserDetails user = User.withDefaultPasswordEncoder().username("user").password("123456").roles("USER").build();
        System.out.println(user.getPassword());

        UsernamePasswordAuthenticationToken token =
            new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        
    }

}
