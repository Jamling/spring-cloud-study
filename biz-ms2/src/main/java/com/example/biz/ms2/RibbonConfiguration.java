package com.example.biz.ms2;

import org.springframework.context.annotation.Bean;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.NoOpLoadBalancer;
import com.netflix.loadbalancer.RandomRule;

public class RibbonConfiguration {

    @Bean
    IRule rule() {
        return new RandomRule();
    }

    ILoadBalancer loadBalancer() {
        return new NoOpLoadBalancer();
    }
}
