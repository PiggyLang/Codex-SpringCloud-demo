# 阶段一：初始化 Spring Cloud 多模块项目

- 历史提交：`0d6e5e690fb24ac23f3ef81cab7cf2db57758b8d`（`0d6e5e6`）
- 父提交：该提交是仓库根提交，没有父提交可比较。
- 目标：建立 Maven 聚合项目和微服务模块骨架。

本文描述的是上述提交对应的历史快照，不代表当前工作树代码。

## 关键变化

新增根 `pom.xml`、`common-api`、`user-service`、`order-service`、`gateway-service` 四个模块的 POM，以及三个服务的 Spring Boot 启动类和上下文测试。根 POM 使用 Java 17、Spring Boot 3.5.16、Spring Cloud 2025.0.3、Spring Cloud Alibaba 2025.0.0.0，并集中管理依赖版本。

此时服务仅依赖 `spring-boot-starter`，没有 Web starter，也没有 HTTP Controller。运行服务启动类只会验证应用上下文并正常退出；不能据此认为已有可访问的 HTTP 服务。

## 知识点

- Maven 聚合父工程通过 `<modules>` 组织多个子模块；父 POM 的 `dependencyManagement` 统一依赖版本，但不自动把依赖添加到子模块。
- `common-api` 为后续模块共享的数据契约预留位置。
- Spring Boot 启动类负责建立应用上下文。是否提供 HTTP 端点取决于 Web 依赖及 Controller，而不只是存在启动类。

## 当时的启动验证

在仓库根目录执行：

```bash
mvn -pl user-service spring-boot:run
```

也可在 IntelliJ IDEA 中导入根 `pom.xml`，找到 `user-service` 下的 `UserServiceApplication`，运行其 `main` 方法。预期日志包含应用上下文启动完成（例如 `Started UserServiceApplication`），随后进程正常退出；该阶段没有 HTTP 端口可供访问。

## 与后续阶段的区别

这是纯骨架阶段：没有 Nacos 注册、Web API、Feign 客户端或网关路由。后续阶段在此基础上逐步加入这些能力。
