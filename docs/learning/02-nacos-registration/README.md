# 阶段二：接入 Nacos 服务注册

- 历史提交：`b49a04f26d92e9e1538e36366a88045a4ae81649`（`b49a04f`）
- 父提交：`0d6e5e690fb24ac23f3ef81cab7cf2db57758b8d`
- 目标：让用户和订单服务声明服务名及 Nacos 注册中心地址。

本文描述的是上述提交对应的历史快照，不代表当前工作树代码。

## 关键变化

在 `user-service/pom.xml` 和 `order-service/pom.xml` 加入 Nacos Discovery 依赖；新增两个服务的 `src/main/resources/application.yml`，配置 `spring.application.name`、当时的端口 `8081`/`8082` 及 `spring.cloud.nacos.discovery.server-addr: 127.0.0.1:8848`。启动测试检查配置属性，并通过测试属性禁用 Nacos Discovery，避免上下文测试必须连接注册中心。

根 POM 管理 Java 17、Spring Boot 3.5.16、Spring Cloud 2025.0.3、Spring Cloud Alibaba 2025.0.0.0。

## 知识点

- `spring.application.name` 是注册中心中服务实例所属服务的逻辑名称。
- Discovery 客户端向 `server-addr` 指定的地址注册实例；声明地址本身不会启动 Nacos 服务端。
- 配置测试可以验证应用读取到的属性；测试中禁用注册客户端，不等于真实注册流程已联通。

## 当时的启动验证

真实注册需要先在 `127.0.0.1:8848` 提供可用的 Nacos 服务端。该阶段仓库快照没有提供 Docker/Nacos 启动脚本，因此这里不指定镜像、账号、控制台地址或启动参数。前置服务已由使用者另行准备后，在仓库根目录运行：

```bash
mvn -pl user-service spring-boot:run
mvn -pl order-service spring-boot:run
```

也可在 IDEA 中分别运行 `UserServiceApplication` 和 `OrderServiceApplication`。本提交已把两个模块的 `spring-boot-starter` 换成 `spring-boot-starter-web`，因此预期用户、订单服务分别监听 `8081`、`8082`，Nacos 客户端以相应服务名和端口尝试注册；此时尚无 Controller，不能据此期待业务 HTTP 端点。Nacos 服务端可用时，在其实例列表中应能看到 `user-service` 与 `order-service`。可在 IDEA 中运行 `UserServiceApplicationTests` 和 `OrderServiceApplicationTests`，这些测试禁用了 Discovery，预期上下文测试通过并检查各自服务名、端口和地址配置。

## 与前后阶段的区别

相较阶段一增加了 Web 依赖、Discovery 依赖和注册配置，但仍无业务 Web API、服务间调用或网关。阶段三再加入 Controller 与 OpenFeign。端口 `8081`/`8082` 是本阶段历史值，阶段四改为 `18081`/`18082`。
