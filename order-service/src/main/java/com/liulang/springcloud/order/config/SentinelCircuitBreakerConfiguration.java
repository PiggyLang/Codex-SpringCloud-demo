package com.liulang.springcloud.order.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStateChangeObserver;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.liulang.springcloud.order.service.UserLookupService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SentinelCircuitBreakerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SentinelCircuitBreakerConfiguration.class);

    @Value("${demo.sentinel.circuit-breaker.error-ratio:0.5}")
    private double errorRatio;

    @Value("${demo.sentinel.circuit-breaker.minimum-requests:5}")
    private int minimumRequests;

    @Value("${demo.sentinel.circuit-breaker.stat-interval-ms:30000}")
    private int statIntervalMs;

    @Value("${demo.sentinel.circuit-breaker.open-seconds:10}")
    private int openSeconds;

    private final CircuitBreakerStateChangeObserver observer = (from, to, rule, snapshot) -> {
        if (UserLookupService.RESOURCE.equals(rule.getResource())) {
            log.info("Sentinel 资源 {} 熔断状态变化: {} -> {}", rule.getResource(), from, to);
        }
    };

    @PostConstruct
    void configure() {
        EventObserverRegistry.getInstance().addStateChangeObserver(UserLookupService.RESOURCE, observer);
        DegradeRule rule = new DegradeRule(UserLookupService.RESOURCE)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(errorRatio)
                .setMinRequestAmount(minimumRequests)
                .setStatIntervalMs(statIntervalMs)
                .setTimeWindow(openSeconds);
        DegradeRuleManager.loadRules(List.of(rule));
    }

    @PreDestroy
    void cleanup() {
        EventObserverRegistry.getInstance().removeStateChangeObserver(UserLookupService.RESOURCE);
    }
}
