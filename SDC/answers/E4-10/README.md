本题独立参考分支：`refs/heads/exp/e4-10`。仅作学习参考，不向 mall 主线创建或合并 PR。

# E4-10 参考答案：构造器注入

## 参考代码入口

本材料对应 mall `SDC/E4`。参考分支 `exp/e4-10` 与 [Issue #50](https://github.com/meng004/mall/issues/50) 已发布；测试位于 `SDC/E4/src/test/java/com/macro/mall/sdc/e4/CmsPrefrenceAreaInjectionTest.java`，服务实现位于 `mall-admin/src/main/java/com/macro/mall/service/impl/CmsPrefrenceAreaServiceImpl.java`。

## 版本与差异

- 基线：`7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`
- 隔离副本分支：`exp/e4-10`
- 改后 HEAD：`7bf6871080febea5af5d7b4736aafeaba8b58a76`
- checkout：`/private/tmp/e4-10-answer`
- 改动：`CmsPrefrenceAreaServiceImpl` 将 Mapper 字段注入改为 `final` 单参数构造器注入；`SDC/E4` 测试依赖增加 `mall-admin`；新增改后服务测试。
- 可应用补丁：`E4-10.patch`，在 mall 仓库根目录以 `rtk git apply <补丁路径>` 应用。

`CmsPrefrenceAreaServiceImpl.after.java` 是改后完整服务源码。`CmsPrefrenceAreaInjectionTest.before.java` 和 `CmsPrefrenceAreaInjectionTest.after.java` 展示同一行为测试改造前后的对象装配差异；正式提交只包含改后测试。

## 验证

实际环境：OpenJDK 26.0.1、Maven 3.9.16、离线依赖缓存。改前及改后各执行一次下列命令，测试结果均为 1 项通过、0 失败、0 错误、0 跳过，且 Maven reactor 全部成功。完整日志分别见 `tests-before.log`、`tests-after.log`。

```sh
rtk env JAVA_HOME=/opt/homebrew/opt/java/libexec/openjdk.jdk/Contents/Home /opt/homebrew/bin/mvn -o -pl SDC/E4 -am -Dtest=CmsPrefrenceAreaInjectionTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

改前测试通过无参构造和反射设置字段建立对象；改后直接 `new CmsPrefrenceAreaServiceImpl(mapper)`。两者验证空列表、多元素列表的对象与顺序、传给 Mapper 的查询条件，以及独立 `AnnotationConfigApplicationContext` 注册 Mapper 后能创建并调用服务。

## 边界

测试使用 Mockito Mapper，不连接数据库。Spring 验证只注册一个 Mapper bean 和服务类，不启动完整应用或执行项目组件扫描；它证明单构造器可由 Spring 装配，不证明完整生产环境配置。此次不验证数据库内容、事务或端到端 HTTP 行为。
