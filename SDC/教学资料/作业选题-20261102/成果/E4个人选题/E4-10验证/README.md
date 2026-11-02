# E4-10：构造器注入验证

验证源码版本：mall `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。目标为 `mall-admin/src/main/java/com/macro/mall/service/impl/CmsPrefrenceAreaServiceImpl.java`。项目要求 JDK 17 或更高、Maven 和可用依赖；初次构建需要联网下载依赖。

在 `mall/SDC/E4/pom.xml` 的 `<dependencies>` 中加入：

```xml
<dependency>
  <groupId>com.macro.mall</groupId><artifactId>mall-admin</artifactId>
  <version>${project.version}</version><scope>test</scope>
</dependency>
```

把本目录的 `CmsPrefrenceAreaInjectionTest.java` 放入 `mall/SDC/E4/src/test/java/com/macro/mall/sdc/e4/`。在 mall 根目录运行：

```sh
rtk mvn -pl SDC/E4 -am -Dtest=CmsPrefrenceAreaInjectionTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

将服务改为单参数构造器注入并删除字段 `@Autowired`，再用 `改后/CmsPrefrenceAreaInjectionTest.java` 替换测试文件并执行同一命令。改前测试使用反射设置 mock；改后测试直接调用构造器。两者均验证空列表、多元素原顺序、查询条件无筛选或排序，以及独立 Spring 容器中服务装配，预期各 1 项通过。

实测：隔离副本、JDK 26、Maven 离线模式，改前和改后均 1 项通过；`-Dmaven.compiler.proc=full` 用于该 JDK 下启用注解处理。测试未连接数据库，也未启动完整应用。
