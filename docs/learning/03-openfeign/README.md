# 阶段三：实现 OpenFeign 远程调用

- 历史提交：`b0651876999eb8fc90053867b2ea8ec4baf2c64c`（`b065187`）
- 父提交：`b49a04f26d92e9e1538e36366a88045a4ae81649`
- 目标：用服务名调用用户服务，并由订单接口组合返回订单和用户信息。

本文描述的是上述提交对应的历史快照，不代表当前工作树代码。

## 关键变化

加入 `UserDTO`、`OrderDTO`；用户服务增加 `/users/{id}` Controller 和共享 API 依赖（Web 依赖已在阶段二加入）；订单服务增加 `/orders/{id}` Controller、`@FeignClient("user-service")` 的 `UserClient`、OpenFeign、LoadBalancer 和共享 API 依赖，并在启动类启用 Feign 客户端扫描。测试覆盖 Controller 行为。

根 POM 的版本仍为 Java 17、Spring Boot 3.5.16、Spring Cloud 2025.0.3、Spring Cloud Alibaba 2025.0.0.0。

## 知识点

- Feign 接口用服务名和映射注解描述远程调用；服务发现和负载均衡负责把 `user-service` 解析到某个实例。
- `OrderController` 调用 `UserClient` 后组装 `OrderDTO`，把服务间通信封装在客户端接口之后。
- Web 层测试可用 MockMvc 和 Mockito 模拟 Feign 客户端，独立验证订单 JSON，不要求测试时启动另一个服务。

## 当时的启动验证

设置 Java 17 后，先在仓库根目录安装 reactor 中共享的 `common-api`，之后再准备 `127.0.0.1:8848` 的 Nacos 服务端（本仓库该历史阶段没有提供 Nacos 容器启动脚本）：

```bash
mvn clean install
```

安装成功后，再分别于两个终端从仓库根目录启动：

```bash
mvn -pl user-service spring-boot:run
mvn -pl order-service spring-boot:run
```

也可在 IDEA 中分别运行 `UserServiceApplication` 与 `OrderServiceApplication`。预期用户服务监听 `8081`，订单服务监听 `8082`，两个实例注册到 Nacos。使用 `curl` 验证：

```bash
curl http://localhost:8081/users/100
curl http://localhost:8082/orders/1
```

订单响应的历史 JSON 契约为 `{"orderId":1,"productName":"Java 面试题","user":{"id":100,"name":"刘浪"}}`；不包含后续阶段的 `userStatus` 或 `userMessage` 字段。在 IDEA 中运行 `OrderControllerTests` 可验证模拟远程调用时的 JSON 字段。

## 与前后阶段的区别

本阶段首次有可调用的 HTTP Controller，并通过 OpenFeign 完成订单到用户的服务调用。仍没有 Gateway 对外路由；使用的是阶段二端口 `8081`/`8082`，下一阶段改为 `18081`/`18082`。
