package com.liulang.springcloud.user.controller;

import com.liulang.springcloud.user.UserServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = UserServiceApplication.class,
        properties = {
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.config.import-check.enabled=false",
                "spring.config.import=",
                "app.message=hello from test configuration"
        }
)
@AutoConfigureMockMvc
class ConfigControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private RefreshScope refreshScope;

    @Test
    void returnsMessageFromConfiguration() throws Exception {
        mockMvc.perform(get("/config/message"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("hello from test configuration"));
    }

    @Test
    void returnsUpdatedMessageAfterRefreshWithoutRestartingApplication() throws Exception {
        mockMvc.perform(get("/config/message"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("hello from test configuration"));

        String propertySourceName = "updated-message";
        environment.getPropertySources().addFirst(new MapPropertySource(
                propertySourceName, Map.of("app.message", "配置已更新")
        ));

        try {
            refreshScope.refreshAll();

            mockMvc.perform(get("/config/message"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("配置已更新"));
        } finally {
            environment.getPropertySources().remove(propertySourceName);
            refreshScope.refreshAll();
        }
    }
}
