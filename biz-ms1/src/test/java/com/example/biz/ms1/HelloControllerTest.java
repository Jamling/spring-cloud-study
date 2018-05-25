package com.example.biz.ms1;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * 使用MockMvc + jsonPath来测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HelloControllerTest {

    @Autowired
    MockMvc mvc;

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testIndex() throws Exception {
        // @formatter:off
        mvc.perform(MockMvcRequestBuilders.get("http://biz-ms1/")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo("Greetings from Spring Boot!")));
        // @formatter:on
    }

    @Test
    public void testHello() throws Exception {
        // @formatter:off
        mvc.perform(MockMvcRequestBuilders.get("/hello")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.msg").value("hello guest"));
        
        mvc.perform(MockMvcRequestBuilders.get("/hello?name=Jamling")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.msg").value("hello Jamling"));
        // @formatter:on
    }
}
