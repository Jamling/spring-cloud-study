package com.example.arch.eureka;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ArchDiscoveryApplicationTests {

    @Autowired
    MockMvc mvc;

    @Test
    public void contextLoads() throws Exception {
        // @formatter:off
        mvc.perform(MockMvcRequestBuilders.get("/")
            .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk());
        // @formatter:on
    }

}
