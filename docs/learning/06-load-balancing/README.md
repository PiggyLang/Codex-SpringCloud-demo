# 阶段六：用户实例日志与网关 DNS 依赖修复

- 历史提交：`45976e3345dc2898472030aba8fff851f736b8c1`（`45976e3`）
- 父提交：`1b45a96523c05d01c4f02b2ed4e6911ea99d1077`
- 目标：记录实际处理请求的用户服务实例端口，并补充网关在 macOS ARM64 上使用的 Netty DNS resolver 依赖。

本文描述的是上述提交对应的历史快照，不代表当前工作树代码。

## 关键变化

用户 Controller 注入 `${server.port}` 并在处理 `/users/{id}` 时记录端口和用户 ID；响应仍是 `new UserDTO(id, "刘浪")`，没有将实例端口加进 DTO。Gateway POM 新增 `netty-resolver-dns-native-macos`，classifier 为 `osx-aarch_64`，scope 为 `runtime`。这是该历史变更明确声明的 macOS ARM64 原生依赖。

## 知识点

- 服务端本地日志能观察一次负载均衡调用实际落到哪个实例。
- 多实例验证需要同一服务名下注册多个端口不同的实例，再通过逻辑服务名多次请求；仅有客户端负载均衡依赖不代表已经观察到均匀分流。
- 本次记录的实例端口属于服务端日志信息，并未改变 `UserDTO` 的 API 字段。

## 当时的启动验证

设置 Java 17 后，在仓库根目录先安装共享 `common-api` 依赖一次，再确保 Nacos 可从 `127.0.0.1:8848` 访问；该阶段历史仓库快照没有 Nacos/Docker 启动脚本。

```bash
mvn clean install
```

安装成功后，启动订单服务与网关：

```bash
mvn -pl order-service spring-boot:run
mvn -pl gateway-service spring-boot:run
```

然后在两个终端分别启动同一个用户服务模块，其中一个使用默认 `18081`，另一个覆盖端口为 `18083`：

```bash
mvn -pl user-service spring-boot:run
mvn -pl user-service spring-boot:run -Dspring-boot.run.arguments=--server.port=18083
```

IDEA 中可创建两个 `UserServiceApplication` 运行配置，第二个添加程序参数 `--server.port=18083`。预期两个实例以同一服务名 `user-service` 注册。多次请求 `GET http://localhost:18080/orders/1` 后，用户服务控制台应记录类似 `user-service 实例端口=18081，处理用户查询 id=100` 和端口 `18083` 的日志；具体分配顺序由负载均衡决策，不承诺每次交替。响应的 `user` 仍只有历史 DTO 中定义的 ID、姓名字段，不包含服务端口。

## 与前后阶段的区别

阶段五已有 Gateway 路由，此阶段未改路由、默认端口或 DTO 结构。它增加服务端日志以观察实例选择，并给 Gateway 增加特定平台的 Netty DNS resolver 运行时依赖。以上均是截至本提交的历史行为；仓库后续阶段还可能增加其它配置和字段，应以当前源码为准。
