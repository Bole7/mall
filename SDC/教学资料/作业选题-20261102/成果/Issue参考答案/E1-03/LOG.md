# E1-03 工作日志

- 基线：`7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`
- 临时 checkout：`/private/tmp/mall-e1-03`，detached HEAD；原仓库未修改。
- 新增测试：`SDC/E1/src/test/java/com/macro/mall/sdc/e1/MemberCollectionServiceTest.java`，6 个 `@Test` 方法。
- 测试资源：`SDC/E1/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` 使用仓库已有的 `mock-maker-subclass` 模式，避免受限环境的 agent attach 失败。
- 测试改动：`SDC/E1/pom.xml` 仅以 test scope 引入 `mall-portal`，复用现有 JUnit/Mockito 依赖。
- 产出补丁：`SDC/E1-03.patch`。
- 运行命令：见 `README.md`。
- Fresh 结果：JDK 17、离线 Maven 构建成功；`MallFactsTest` 3 项 + `MemberCollectionServiceTest` 6 项，共 9 项，0 failure、0 error、0 skipped。
- 先行失败记录：默认 inline mock maker 在本机无法自附加 Byte Buddy agent；改用仓库已有 subclass 配置后通过。
- 限制：未安装/启动 MongoDB；未连接 MySQL；未验证 Spring `@Value` 绑定、Mongo 索引/分页、事务、并发唯一性、认证链或 HTTP 响应。

