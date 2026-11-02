本题独立参考分支：`refs/heads/exp/e4-12`。仅作学习参考，不向 mall 主线创建或合并 PR。

# E4-12 AOP 事务代理参考答案

## 变更

在 `SDC/E4/pom.xml` 增加 `mall-admin` 的 test-scope 依赖，并将 `BrandTransactionTest.java` 加入 E4 测试模块。产品源码、事务传播级别、回滚规则和业务顺序均不改。可在 mall 仓库根目录应用 `E4-12.patch`：

```sh
rtk git apply '/path/to/E4-12.patch'
```

## 验证

2026-11-02 在隔离 worktree `/private/tmp/e4-12-aop-fresh` 验证。`HEAD` 与 `base` 均为 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`，detached HEAD；原工作目录未改。

环境为 macOS arm64、Maven 3.9.16、Homebrew OpenJDK 26.0.1。该会话没有默认 `JAVA_HOME`，所以测试调用显式设置为 `/opt/homebrew/opt/java/libexec/openjdk.jdk/Contents/Home`。依赖使用本地缓存，执行命令：

```sh
rtk /usr/bin/env JAVA_HOME=/opt/homebrew/opt/java/libexec/openjdk.jdk/Contents/Home mvn -o -pl SDC/E4 -am -Dtest=BrandTransactionTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

结果：`Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`。完整输出见 [fresh-test.log](fresh-test.log)。

## 证据边界

测试从 Spring 容器取得代理，正常路径验证事务管理器收到提交；第二次 Mapper 写入抛出运行时异常时，验证异常传播、收到回滚且没有提交。直接构造的实现对象调用同一业务方法，不经过代理，因此事务管理器无交互。

测试替身只证明 Spring 发出了提交或回滚动作，不证明真实数据库中的数据已提交或回滚，也不覆盖完整应用装配。额外的自调用用测试子类构造对照情境，用于说明代理边界；它不是生产源码有自调用缺陷的证据。
