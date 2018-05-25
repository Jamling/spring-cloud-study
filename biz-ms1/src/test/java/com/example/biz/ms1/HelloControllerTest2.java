package com.example.biz.ms1;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 使用RestTemplate来测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloControllerTest2 {

    @LocalServerPort
    private int port;

    private String base;

    @Autowired
    private TestRestTemplate template;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/").toString();
        // 如果是微服务消费方，可以使用微服务名字来调用
        // this.base = new URL("http://biz-ms1/").toString();
    }

    @Test
    public void testIndex() {
        // 使用 xxxForEntity
        ResponseEntity<String> response = template.getForEntity(base, String.class);
        assertThat(response.getBody(), equalTo("Greetings from Spring Boot!"));
    }

    @Test
    public void testHello() {
        // 使用RestTemplate.xxxForObject，将json转为java对象
        Info ret = template.getForObject(base + "hello", Info.class);
        assertEquals("hello guest", ret.msg);

        // 带url参数变量
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "Jamling");
        ret = template.getForObject(base + "hello?name={name}", Info.class, vars);
        assertEquals("hello Jamling", ret.msg);
    }

    static class Info {
        public int code;
        public String msg;
    }
}
