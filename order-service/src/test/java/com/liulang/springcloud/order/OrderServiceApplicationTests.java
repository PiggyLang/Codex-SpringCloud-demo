package com.liulang.springcloud.order;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.liulang.springcloud.order.service.UserLookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = OrderServiceApplication.class,
        properties = "spring.cloud.nacos.discovery.enabled=false"
)
class OrderServiceApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    void applicationContextStarts() {
    }

    @Test
    void serviceRegistrationConfigurationIsLoaded() {
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("order-service");
        assertThat(environment.getProperty("server.port")).isEqualTo("18082");
        assertThat(environment.getProperty("spring.cloud.nacos.discovery.server-addr"))
                .isEqualTo("127.0.0.1:8848");
    }

    @Test
    void localSentinelRuleProtectsOnlyTheNamedOutboundResource() {
        DegradeRule rule = DegradeRuleManager.getRulesOfResource(UserLookupService.RESOURCE).iterator().next();

        assertEquals(0.5, rule.getCount());
        assertEquals(5, rule.getMinRequestAmount());
        assertEquals(30_000, rule.getStatIntervalMs());
        assertEquals(10, rule.getTimeWindow());
        assertEquals("false", environment.getProperty("spring.cloud.sentinel.filter.enabled"));
    }
}
