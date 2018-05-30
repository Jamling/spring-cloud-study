package com.example.arch.oauth;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthorizationServerConfigurationTest {

    @Autowired
    private TestRestTemplate template;

    private String base;

    @Before
    public void setUp() throws Exception {
        base = "http://localhost:1111/oauth/";
    }

    @Test
    public void test() {
        String passwordUrl = String.format("%stoken?grant_type=password&username=%s&password=%s", "client1", "client1");
        TokenInfo info = template.getForObject(passwordUrl, TokenInfo.class);
        System.out.println(info);
    }

    public static class TokenInfo extends HashMap<String, Object> {

    }
}
