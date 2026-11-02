# E4-11：MyBatis 结果映射验证

验证源码版本：mall `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。入口是 `OmsOrderReturnApplyDao#getDetail` 和 `mall-admin/src/main/resources/dao/OmsOrderReturnApplyDao.xml` 的 `returnApplyDetailResultMap`。项目要求 JDK 17 或更高、Maven 和可用依赖；初次构建需要联网。

在 `mall/SDC/E4/pom.xml` 的 `<dependencies>` 中加入：

```xml
<dependency>
  <groupId>com.macro.mall</groupId><artifactId>mall-admin</artifactId>
  <version>${project.version}</version><scope>test</scope>
</dependency>
<dependency>
  <groupId>com.h2database</groupId><artifactId>h2</artifactId>
  <version>2.1.214</version><scope>test</scope>
</dependency>
```

把 `ReturnApplyMappingTest.java` 放入 `mall/SDC/E4/src/test/java/com/macro/mall/sdc/e4/`，在 mall 根目录运行：

```sh
rtk mvn -pl SDC/E4 -am -Dtest=ReturnApplyMappingTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

测试使用 H2 内存数据库和真实 MyBatis XML，验证 ID 参数绑定、有地址时 `ca_` 前缀的关联映射、无地址及 ID 未命中，预期 1 项通过。实测为隔离副本、JDK 26、Maven 离线模式；联网执行上面的命令不需要添加下列仓库配置。本机离线缓存的 H2 来自 `huaweicloud` 仓库 ID，因此离线复现时在 `SDC/E4/pom.xml` 的 `<project>` 内额外加入：

```xml
<repositories><repository><id>huaweicloud</id><url>https://repo.huaweicloud.com/repository/maven/</url></repository></repositories>
```

此测试未验证完整应用、生产 MySQL 或事务。
