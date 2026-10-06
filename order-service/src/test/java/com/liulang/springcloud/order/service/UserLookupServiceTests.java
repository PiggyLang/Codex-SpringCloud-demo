package com.liulang.springcloud.order.service;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStateChangeObserver;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.liulang.springcloud.api.UserDTO;
import com.liulang.springcloud.order.client.UserClient;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserLookupServiceTests {

    private static final String RESOURCE = "user-info";
    private static final String OBSERVER_NAME = "user-lookup-service-tests";
    private final List<String> transitions = new ArrayList<>();
    private final CircuitBreakerStateChangeObserver observer = (from, to, rule, snapshot) ->
            transitions.add(from + "->" + to);

    @BeforeEach
    void installRealSentinelRule() {
        EventObserverRegistry.getInstance().addStateChangeObserver(OBSERVER_NAME, observer);
        DegradeRuleManager.loadRules(List.of(new DegradeRule(RESOURCE)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.5)
                .setMinRequestAmount(5)
                .setStatIntervalMs(30_000)
                .setTimeWindow(1)));
    }

    @AfterEach
    void clearGlobalSentinelState() {
        DegradeRuleManager.loadRules(List.of());
        EventObserverRegistry.getInstance().removeStateChangeObserver(OBSERVER_NAME);
    }

    @Test
    void opensBlocksDownstreamAndHalfOpenProbeCanReopenOrRecover() throws Exception {
        AtomicInteger downstreamCalls = new AtomicInteger();
        AtomicInteger failedCalls = new AtomicInteger();
        UserClient fake = id -> {
            downstreamCalls.incrementAndGet();
            int call = failedCalls.getAndIncrement();
            if (call < 4 || call == 5) {
                throw remoteFailure();
            }
            if (call == 4) return null;
            return new UserDTO(id, "刘浪");
        };
        UserLookupService service = new UserLookupService(fake);

        for (int i = 0; i < 4; i++) {
            assertThrows(FeignException.class, service::getUser);
        }
        assertNull(service.getUser()); // A null body is counted as a failed remote call.
        assertEquals(5, downstreamCalls.get());
        assertThrows(BlockException.class, service::getUser);
        assertEquals(5, downstreamCalls.get()); // OPEN rejects before the client is invoked.

        assertThrows(FeignException.class, () -> awaitProbe(service, downstreamCalls, 6)); // Failed probe reopens it.
        assertEquals(6, downstreamCalls.get());
        assertThrows(BlockException.class, service::getUser);
        assertEquals(6, downstreamCalls.get());

        assertEquals(new UserDTO(100L, "刘浪"), awaitProbe(service, downstreamCalls, 7)); // Success closes it.
        assertEquals(7, downstreamCalls.get());
        assertEquals(new UserDTO(100L, "刘浪"), service.getUser());
        assertEquals(8, downstreamCalls.get());
        assertEquals(List.of("CLOSED->OPEN", "OPEN->HALF_OPEN", "HALF_OPEN->OPEN",
                "OPEN->HALF_OPEN", "HALF_OPEN->CLOSED"), transitions);
    }

    private static UserDTO awaitProbe(UserLookupService service, AtomicInteger downstreamCalls, int expectedCalls)
            throws InterruptedException {
        long deadline = System.nanoTime() + 3_000_000_000L;
        while (System.nanoTime() < deadline) {
            try {
                UserDTO user = service.getUser();
                if (downstreamCalls.get() == expectedCalls) return user;
            } catch (BlockException stillOpen) {
                Thread.sleep(20);
            }
        }
        throw new AssertionError("Sentinel did not admit a half-open probe before the deadline");
    }

    private static FeignException remoteFailure() {
        Request request = Request.create(Request.HttpMethod.GET, "http://user-service/users/100", Map.of(), null,
                StandardCharsets.UTF_8, null);
        Response response = Response.builder().status(503).reason("Service Unavailable").request(request).build();
        return FeignException.errorStatus("UserClient#getUser", response);
    }
}
