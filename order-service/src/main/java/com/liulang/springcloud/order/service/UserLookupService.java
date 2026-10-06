package com.liulang.springcloud.order.service;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.liulang.springcloud.api.UserDTO;
import com.liulang.springcloud.order.client.UserClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class UserLookupService {

    public static final String RESOURCE = "user-info";
    private static final Logger log = LoggerFactory.getLogger(UserLookupService.class);

    private final UserClient userClient;

    public UserLookupService(UserClient userClient) {
        this.userClient = userClient;
    }

    public UserDTO getUser() throws BlockException {
        Entry entry = null;
        try {
            entry = SphU.entry(RESOURCE, EntryType.OUT);
            log.info("调用用户服务查询用户 100");
            UserDTO user = userClient.getUser(100L);
            if (user == null) {
                Tracer.traceEntry(new EmptyUserResponseException(), entry);
            }
            return user;
        } catch (FeignException exception) {
            if (entry != null) {
                Tracer.traceEntry(exception, entry);
            }
            throw exception;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private static final class EmptyUserResponseException extends RuntimeException {
        private EmptyUserResponseException() {
            super("用户服务返回空响应");
        }
    }
}
