package com.liulang.springcloud.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = UserServiceApplication.class,
        properties = {
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.config.import-check.enabled=false",
                "spring.config.import="
        }
)
@AutoConfigureMockMvc
class UserServiceApplicationTests {

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void applicationContextStarts() {
    }

    @Test
    void serviceRegistrationConfigurationIsLoaded() {
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("user-service");
        assertThat(environment.getProperty("server.port")).isEqualTo("18081");
        assertThat(environment.getProperty("spring.cloud.nacos.discovery.server-addr"))
                .isEqualTo("127.0.0.1:8848");
    }

    @Test
    void returnsLocalMessageWhenRemoteConfigurationIsDisabled() throws Exception {
        mockMvc.perform(get("/config/message"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("hello from local config"));
    }
}
