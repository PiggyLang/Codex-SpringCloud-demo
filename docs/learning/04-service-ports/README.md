# 阶段四：调整服务默认端口

- 历史提交：`1ae624466038e0cc7c65a0b178c9f77e49dae4fc`（`1ae6244`）
- 父提交：`b0651876999eb8fc90053867b2ea8ec4baf2c64c`
- 目标：将用户和订单服务默认端口从 8081/8082 调整到 18081/18082。

本文描述的是上述提交对应的历史快照，不代表当前工作树代码。

## 关键变化

修改 `user-service/src/main/resources/application.yml` 与 `order-service/src/main/resources/application.yml` 的 `server.port`，并同步更新两个服务启动测试中的预期端口。其它服务发现与 OpenFeign 行为延续阶段三。

根 POM 仍指定 Java 17、Spring Boot 3.5.16、Spring Cloud 2025.0.3、Spring Cloud Alibaba 2025.0.0.0。

## 知识点

- 服务端口是实例监听地址的一部分；服务发现注册实例时会发布可用地址，消费者使用逻辑服务名而非固定端口发起调用。
- 修改配置后也要同步测试断言，避免测试仍验证旧配置。

## 当时的启动验证

设置 Java 17 后，在仓库根目录先安装共享 `common-api` 依赖一次：

```bash
mvn clean install
```

确保 Nacos 可在配置地址 `127.0.0.1:8848` 使用后，分别启动两个服务：

```bash
mvn -pl user-service spring-boot:run
mvn -pl order-service spring-boot:run
```

或在 IDEA 中分别运行 `UserServiceApplication` 和 `OrderServiceApplication`。预期用户服务监听 `18081`、订单服务监听 `18082`；用户端点 `GET /users/100` 可直接访问。订单端点 `GET /orders/1` 返回阶段三所述历史 JSON 契约，字段为 `orderId`、`productName`、`user.id`、`user.name`，没有后续阶段的 `userStatus` 或 `userMessage`。订单调用仍按 `user-service` 服务名发现实例。

## 与前后阶段的区别

此提交只调整用户、订单服务端口，没有引入 Gateway；订单接口仍直接在 `18082` 提供。下一阶段增加监听 `18080` 的 Gateway，并将 `/orders/**` 转发到订单服务。
