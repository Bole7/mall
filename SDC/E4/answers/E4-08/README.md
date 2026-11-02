# E4-08 管理员与缓存循环依赖

参考分支：[exp/e4-08](https://github.com/meng004/mall/tree/exp/e4-08)；答案目录：[SDC/E4/answers/E4-08](https://github.com/meng004/mall/tree/exp/e4-08/SDC/E4/answers/E4-08)。仅作学习参考，不创建 PR，不合入 mall 主线。

## 结论

采用缓存实现直接依赖 `UmsAdminMapper`。`delAdmin(id)` 仍在失效时按 ID 查询管理员并用用户名拼出原缓存 key；管理员不存在时仍不调用 Redis。管理员业务服务以构造器注入 `UmsAdminCacheService`，删除 `UmsAdminService.getCacheService()` 和 `SpringUtil` 定位。

## 依赖与接缝

改前：

```text
UmsAdminServiceImpl --(SpringUtil 定位)--> UmsAdminCacheServiceImpl
UmsAdminCacheServiceImpl --> UmsAdminService --> UmsAdminMapper
```

改后：

```text
UmsAdminServiceImpl --> UmsAdminCacheService
UmsAdminCacheServiceImpl --> UmsAdminMapper
UmsAdminServiceImpl --> UmsAdminMapper
```

没有服务层回边，也不再通过全局上下文定位缓存服务。管理员服务的构造器明确暴露缓存协作者，测试可直接传入替身。公开 HTTP 路径未改。

## 方案比较与代价

方案 A（采用）：缓存服务注入 `UmsAdminMapper`。删除逻辑只需读取用户名，Mapper 是该数据的直接访问边界；保留原查询时机、ID 参数和不存在时跳过 Redis 的分支。代价是缓存服务直接依赖持久层 Mapper，和 portal 的会员缓存实现一致。

方案 B：把用户名传入 `delAdmin(id, username)`。这会让每个失效调用者都需携带并保证用户名正确；删除、改密、更新等路径当前并非都天然持有可信用户名。接口更复杂，也可能在用户名修改场景用旧/新值失效错误 key。

不采用方案 B，因为该失效操作需要通过 ID 得到 key 所需用户名，直接 Mapper 查询是最小正确方案。

既有行为的限制：删除先删除数据库记录再查询用户名；查不到时不会删除管理员缓存。更新用户名后失效查询读取更新后的用户名，可能留下旧用户名对应的缓存。本次按题目要求保留这些行为，并不表示它们符合所有业务预期。

## 行为与证据

- `AdminCacheWiringTest` 同一份测试改前、改后均为 4 项通过：更新、删除、改密仍调用相同失效方法；删除先落 Mapper 再按原顺序删除管理员与资源缓存；缓存服务用查询到的用户名删除 `mall:admin:ada`，管理员不存在时不访问 Redis；删除后查询不到管理员时只删除资源缓存。测试通过反射适配两版构造器和缓存查询，旧版为 `SpringUtil` 设置上下文替身。
- 同一测试以真实 Spring `AnnotationConfigApplicationContext` 装配管理员服务、缓存服务和 Mapper/Redis/JWT/密码编码替身，容器启动成功。旧版也能启动，因为服务定位器延迟了回边；容器启动本身不证明旧版没有逻辑回边。
- `before-feature-tests.log` 与 `after-feature-tests.log` 是同一测试前后完整日志；`before-mall-admin-test.log` 与 `after-mall-admin-test.log` 是同一编译命令的日志，均成功并跳过测试。`before-mall-admin-build.log` 与 `after-mall-admin-build.log` 保留了额外的 package 尝试：均在 Docker 镜像阶段因无权限失败，不能称为打包成功。
- 测试中的 Mapper、Redis 是 Mockito 替身；未声称验证真实数据库或 Redis。

## 版本基线

本次隔离 checkout：`/private/tmp/mall-e408`。本次 base：`7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`；它与线上题面标记的历史 Issue 基线 `baseline-dcaa93b3` 不同。前后日志均从本次 base 产生，不能把它们描述成历史 Issue 基线的结果。

## 学生复现

从仓库根目录执行，JDK 17：

```bash
git clone --branch exp/e4-08 --single-branch https://github.com/meng004/mall.git mall-e4-08
cd mall-e4-08
mvn -pl SDC/E4 -am -DskipTests=false -Dtest=AdminCacheWiringTest -Dsurefire.failIfNoSpecifiedTests=false test
```

## 本轮运行记录

本轮隔离 checkout 使用 JDK 17 与 RTK 包装的等价命令，保留完整历史日志：

```bash
rtk env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home/bin:$PATH rtk proxy mvn -pl SDC/E4 -am -DskipTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dtest=AdminCacheWiringTest test
```

结果：4 tests, 0 failures, 0 errors, 0 skipped。学生命令不依赖本机绝对路径。
