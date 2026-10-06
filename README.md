# Spring Cloud 学习记录

本仓库按提交逐步演进。下面六份文档记录早期阶段的历史快照和当时的验证方式；当前项目还包含这些提交之后的功能与配置。请以当前源码及配置为准启动完整项目，历史命令仅用于理解对应阶段，不能直接当作当前完整功能的操作说明。

## 六阶段历史快照

1. [阶段一：初始化多模块项目](docs/learning/01-initialization/README.md)
2. [阶段二：接入 Nacos 服务注册](docs/learning/02-nacos-registration/README.md)
3. [阶段三：实现 OpenFeign 远程调用](docs/learning/03-openfeign/README.md)
4. [阶段四：调整服务默认端口](docs/learning/04-service-ports/README.md)
5. [阶段五：接入 Gateway 订单路由](docs/learning/05-gateway/README.md)
6. [阶段六：增加用户实例日志并修复网关 DNS 依赖](docs/learning/06-load-balancing/README.md)

## 模块文档

- [user-service：Nacos Config 动态配置说明](user-service/README.md)
- [order-service：Sentinel 熔断说明](order-service/README.md)
