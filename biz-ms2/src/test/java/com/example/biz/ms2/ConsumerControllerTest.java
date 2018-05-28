package com.example.biz.ms2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConsumerControllerTest {
    @LocalServerPort
    private int port;

    private String base;

    @Autowired
    private TestRestTemplate template;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/").toString();
    }

    @Test
    public void testIndex() {
        ResponseEntity<String> response = template.getForEntity(base, String.class);
        assertThat(response.getBody(), equalTo("Greetings from Spring Boot!"));
    }
    
//    @Test
//    public void testRibbon() {
//        ResponseEntity<Integer> response = template.getForEntity(base + "port", Integer.class);
//        System.out.println("ribbon call biz-ms1:" + response.getBody());
//        assertTrue(Arrays.asList(8080, 8079).contains(response.getBody()));
//    }
}
