package com.liulang.springcloud.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = GatewayServiceApplication.class,
        properties = "spring.cloud.nacos.discovery.enabled=false"
)
class GatewayServiceApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    void applicationContextStarts() {
    }

    @Test
    void orderRouteConfigurationIsLoaded() {
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("gateway-service");
        assertThat(environment.getProperty("server.port")).isEqualTo("18080");
        assertThat(environment.getProperty("spring.cloud.nacos.discovery.server-addr"))
                .isEqualTo("127.0.0.1:8848");
        assertThat(environment.getProperty("spring.cloud.gateway.server.webflux.routes[0].id"))
                .isEqualTo("order-service-route");
        assertThat(environment.getProperty("spring.cloud.gateway.server.webflux.routes[0].uri"))
                .isEqualTo("lb://order-service");
        assertThat(environment.getProperty("spring.cloud.gateway.server.webflux.routes[0].predicates[0]"))
                .isEqualTo("Path=/orders/**");
    }
}
