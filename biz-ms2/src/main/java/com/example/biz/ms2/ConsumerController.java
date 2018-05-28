package com.example.biz.ms2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ConsumerController {
    private static Logger log = LoggerFactory.getLogger(ConsumerController.class);
    @Autowired
    RestTemplate template;

    @RequestMapping(path = "/")
    public String index() {
        String welcome = template.getForObject("http://biz-ms1", String.class);
        log.error("welcome: " + welcome);
        return welcome;
    }
}
