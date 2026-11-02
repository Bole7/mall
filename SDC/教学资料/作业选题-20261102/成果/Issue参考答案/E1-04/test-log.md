# E1-04 验证日志

- 临时 checkout：`/private/tmp/mall-e1-04`
- 基线：`7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`
- 测试代码：`SDC/E1/src/test/java/com/macro/mall/sdc/e1/MemberReadHistoryBehaviorTest.java`
- JDK：Homebrew OpenJDK `26.0.1`；项目 compiler 以 `--release 17` 编译。
- fresh 命令：

```bash
rtk proxy env JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home/bin:/opt/homebrew/bin:/usr/bin mvn -f pom.xml -pl SDC/E1 -am clean test -DskipTests=false -Dtest=MemberReadHistoryBehaviorTest,MallFactsTest -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.compiler.proc=full
```

- 结果：`BUILD SUCCESS`；`MemberReadHistoryBehaviorTest` 5 项、`MallFactsTest` 3 项通过，0 failure、0 error、0 skipped。Reactor 中 mall-common、mall-mbg、mall-security、mall-portal、SDC 和 SDC/E1 均成功。
- 边界：通过 Mockito 验证真实 service 实现与下游调用参数；未运行 mall-portal 的数据库集成测试。完整 `clean test` 曾在 portal 集成测试因缺少本地应用环境配置而失败，故此次只选择 E1 单元测试。
- JDK 26 的初始 fresh 编译需要 `-Dmaven.compiler.proc=full` 才能触发项目 Lombok 注解处理；Mock 测试使用 Mockito subclass mock maker，避免 JDK 26 外部 agent attach 依赖。
