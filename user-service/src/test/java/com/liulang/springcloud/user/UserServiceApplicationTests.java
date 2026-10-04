package com.liulang.springcloud.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = UserServiceApplication.class,
        properties = "spring.cloud.nacos.discovery.enabled=false"
)
class UserServiceApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    void applicationContextStarts() {
    }

    @Test
    void serviceRegistrationConfigurationIsLoaded() {
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("user-service");
        assertThat(environment.getProperty("server.port")).isEqualTo("8081");
        assertThat(environment.getProperty("spring.cloud.nacos.discovery.server-addr"))
                .isEqualTo("127.0.0.1:8848");
    }
}
