# 阶段五：接入 Gateway 订单路由

- 历史提交：`1b45a96523c05d01c4f02b2ed4e6911ea99d1077`（`1b45a96`）
- 父提交：`1ae624466038e0cc7c65a0b178c9f77e49dae4fc`
- 目标：让 Gateway 在 `18080` 接收请求，并用服务名将订单路径路由到订单服务。

本文描述的是上述提交对应的历史快照，不代表当前工作树代码。

## 关键变化

Gateway 模块加入 Spring Cloud Gateway WebFlux、Spring Cloud LoadBalancer、Nacos Discovery 依赖；`application.yml` 配置服务名、Nacos 地址、端口 `18080` 和 `lb://order-service` 路由，匹配 `/orders/**`。网关测试检查路由属性，并禁用 Discovery 以便独立加载上下文。

## 知识点

- `Path=/orders/**` 是路由谓词，匹配路径后由 Gateway 选择对应路由。
- `lb://order-service` 表示通过服务名和负载均衡解析目标实例，而不是写死某台主机端口。
- Gateway 使用 WebFlux 运行栈；其配置与 MVC 服务的 Web 依赖不同。

## 当时的启动验证

设置 Java 17 后，在仓库根目录先安装共享 `common-api` 依赖一次，再准备位于 `127.0.0.1:8848` 的 Nacos 服务端：

```bash
mvn clean install
```

安装成功后，在三个终端从仓库根目录分别运行：

```bash
mvn -pl user-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl gateway-service spring-boot:run
```

IDEA 中可分别运行三个模块的 `UserServiceApplication`、`OrderServiceApplication`、`GatewayServiceApplication`。预期服务监听端口依次为 `18081`、`18082`、`18080`，且 Discovery 可用时注册到 Nacos。请求 `GET http://localhost:18080/orders/1` 应由网关转到订单服务，再由订单服务通过 Feign 获取用户信息，响应遵循阶段三记录的历史 JSON 契约。运行 `GatewayServiceApplicationTests` 可验证路由配置属性。

## 与前后阶段的区别

相较阶段四，新增了外部入口 Gateway 和订单路由；底层用户、订单服务端口仍为 `18081`/`18082`。本阶段不包含后续的用户实例端口日志；下一阶段才加入该日志与 macOS ARM64 Netty DNS resolver 运行时依赖。
