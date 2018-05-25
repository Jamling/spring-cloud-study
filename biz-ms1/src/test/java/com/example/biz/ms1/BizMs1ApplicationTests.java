package com.example.biz.ms1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BizMs1ApplicationTests {
    @Autowired
    DiscoveryClient client;

    @Test
    public void contextLoads() {
        // 测试Eureka Client，如果biz-ms1微服务已启动，则测试通过
        List<ServiceInstance> instances = client.getInstances("biz-ms1");
        if (instances == null || instances.isEmpty()) {
            fail("Please confirm you started biz-ms1 and register to eureka server");
        } else {
            ServiceInstance instance = instances.get(0);
            System.out.println(instance);
            String uri = instance.getUri().toString();
            String host = instance.getHost();
            int port = instance.getPort();
            assertNotNull(uri);
            assertNotNull(host);
            assertEquals(8080, port);
        }

    }

}
