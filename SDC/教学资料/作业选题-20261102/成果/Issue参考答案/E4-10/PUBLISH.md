# E4-10：商品优选列表服务的单依赖构造器注入

> 参考答案；非 PR。参考代码已发布至独立分支。

## 目标

围绕 `CmsPrefrenceAreaServiceImpl` 验证单依赖服务的构造器注入。先阅读实现和已有调用关系，再比较改造前后的装配方式，说明依赖明确性、字段不可变性和测试便利性的变化，并指出构造器注入本身不改变 `listAll()` 的查询语义。

## 参考版本

- 基线：`7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`
- 参考分支：`exp/e4-10`
- 参考实现：`mall-admin/src/main/java/com/macro/mall/service/impl/CmsPrefrenceAreaServiceImpl.java`
- 测试：`SDC/E4/src/test/java/com/macro/mall/sdc/e4/CmsPrefrenceAreaInjectionTest.java`
- 改后参考提交：`7bf6871080febea5af5d7b4736aafeaba8b58a76`

## 验收

从 mall 仓库根目录执行：

```sh
mvn -o -pl SDC/E4 -am -Dtest=CmsPrefrenceAreaInjectionTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

基线反射装配测试和改后构造器装配测试，在 OpenJDK 26.0.1、Maven 3.9.16 的离线缓存环境中分别通过 1 项，失败、错误、跳过均为 0。参考答案目录保留两次完整日志和前后测试源码。

## 限制

测试使用 Mapper mock，不连接数据库；最小 Spring 容器只验证 Mapper bean 与服务 bean 的装配，不启动完整应用，也不证明生产环境扫描配置。参考测试覆盖空/多元素返回及原查询条件，不代表数据库集成或端到端验收。


## 已发布的参考代码

- [完整参考答案、运行说明与证据](https://github.com/meng004/mall/tree/exp/e4-10/SDC/answers/E4-10)
- [独立参考分支](https://github.com/meng004/mall/tree/exp/e4-10)
- 用途：学生学习参考；不创建 PR，不合入 mall 主线。

## 学生复现入口

```sh
git clone --branch exp/e4-10 --single-branch https://github.com/meng004/mall.git mall-e4-10
cd mall-e4-10
mvn -pl SDC/E4 -am -Dtest=CmsPrefrenceAreaInjectionTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

JDK 17+；本轮使用 JDK 26.0.1、Maven 3.9.16 和已有离线缓存。改前、改后各1项测试通过，0失败、0错误、0跳过。首次运行须能下载依赖，不能直接照抄离线 `-o`。参考基线为 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。
