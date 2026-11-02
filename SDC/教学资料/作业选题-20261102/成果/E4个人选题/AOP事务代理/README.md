# Spring 事务代理验证

源码依据：mall `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。`SmsHomeBrandService#create` 的接口方法标有 `@Transactional`，实现逐条调用 mapper 的 `insert`。自调用测试是选做的教学扩展，生产代码没有此处展示的自调用入口，不是生产缺陷。

在 mall 副本中操作：

1. 在 `SDC/E4/pom.xml` 的 `<dependencies>` 内增加：

   ```xml
   <dependency>
     <groupId>com.macro.mall</groupId>
     <artifactId>mall-admin</artifactId>
     <version>${project.version}</version>
     <scope>test</scope>
   </dependency>
   ```

2. 将 `BrandTransactionTest.java` 放入 `SDC/E4/src/test/java/com/macro/mall/sdc/e4/`。
3. 在 mall 根目录运行 `rtk mvn -pl SDC/E4 -am -Dtest=BrandTransactionTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test`。父配置要求 Java 17；首次运行需要 Maven 依赖可用。

本次在隔离副本、JDK 26、已缓存依赖下使用 `rtk mvn -o -pl SDC/E4 -am -Dtest=BrandTransactionTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test`：2 项测试通过。高版本 JDK 可能需要显式开启注解处理；项目编译目标为 Java 17。

测试使用 mock mapper 和 mock 事务管理器，验证代理调用通知提交或回滚，以及测试子类的自调用绕过事务代理；不验证真实数据库回滚、完整应用装配或生产代码存在自调用。
