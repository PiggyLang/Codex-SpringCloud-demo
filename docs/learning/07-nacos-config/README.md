# 阶段七：接入 Nacos Config 动态配置

历史提交：`3ea8ef45851fb7a1cc96f139689510c93f8a7e81`。

本文记录该提交对应的历史快照和当时的验证方式；后续阶段增加了更多功能与配置。请以当前源码及配置为准启动完整项目。

注册中心回答“服务在哪里”，配置中心回答“服务使用什么配置”。本阶段只将用户服务的业务字段 `app.message` 放到 Nacos，端口、服务名和连接 Nacos 所需的地址仍保留在本地。

## 1. 在 Nacos 发布配置

打开 <http://localhost:8080/index.html>，进入“配置管理 → 配置列表”，选择 `public` 命名空间并新增配置：

- Data ID：`user-service.yaml`（注意后缀是 `.yaml`，不是 `.yml`）
- Group：`DEFAULT_GROUP`
- 配置格式：YAML
- 配置内容：

```yaml
app:
  message: hello from nacos
```

点击发布。如果已有同名配置，先检查内容，避免覆盖已有配置。

## 2. 在 IDEA 重启两个用户实例

重新加载 Maven 项目，停止并重新启动 `18081` 和 `18083` 两个用户实例，使新依赖和新代码生效。第二个实例继续使用程序参数 `--server.port=18083`，两者的服务名均为 `user-service`。

订单服务和网关不需要修改。本阶段的新接口直接访问用户服务，网关目前只路由 `/orders/**`。

```text
GET http://localhost:18081/config/message
GET http://localhost:18083/config/message
```

两个实例均应返回：

```json
{"message":"hello from nacos"}
```

如果返回 `hello from local config`，说明使用了本地默认值，尚不能据此认定远程配置读取成功。检查命名空间、Data ID、Group 和启动时的配置加载日志。

## 3. 不重启，验证动态刷新

在 Nacos 编辑同一份配置，修改为：

```yaml
app:
  message: 配置已更新
```

点击发布，等待配置变更通知到达，再请求两个实例的 `/config/message`。预期都返回 `{"message":"配置已更新"}`，无需再次重启应用，也无需手动调用刷新接口。

最后请求 `http://localhost:18080/orders/1`，确认原来的网关 → 订单服务 → 用户服务调用链仍正常。

## 代码之间如何协作

- `spring-cloud-starter-alibaba-nacos-config`：接入配置中心，不同于只负责注册发现的 `nacos-discovery`。
- `spring.config.import`：显式指定加载 `DEFAULT_GROUP` 下的 `user-service.yaml`。
- `refreshEnabled=true`：监听这份远程配置的变更。
- `@Value("${app.message}")`：把 Spring 环境中的配置值注入字段。
- `@RefreshScope`：刷新时使这个控制器的实例失效，后续请求重新创建实例并注入新值。仅添加 `@Value` 不能保证字段动态更新。
- `optional:`：远程配置导入失败时允许应用继续启动，接口会使用本地默认值；这不等于注册中心也可以不可用。

不是所有配置都能热更新。这里仅验证业务字段 `app.message`，不尝试动态修改服务端口或其他启动期配置。

## 自动测试的边界

从项目根目录使用 Java 17 执行 `mvn verify`。测试关闭 Nacos 客户端并覆盖配置导入，验证接口响应、本地默认值和刷新作用域下的新值，避免依赖正在运行的 Nacos 或占用真实服务端口。

自动测试不替代上述真实 Nacos 发布与双实例刷新验证。
