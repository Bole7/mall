## E4-08 管理员服务与缓存服务的循环依赖（进阶）

## 靶点与学习目标

- `UmsAdminCacheServiceImpl` 注入 `UmsAdminService`，`delAdmin` 按 ID 调用 `getItem` 回查用户名。
- `UmsAdminServiceImpl` 通过 `SpringUtil.getBean` 定位缓存服务，掩盖了服务间回边；`UmsAdminService` 暴露的 `getCacheService()` 没有外部调用者。
- 对照 portal 的 `UmsMemberCacheServiceImpl` 直接使用 `UmsMemberMapper`。
- 比较依赖注入与服务定位器、接口隔离及测试接缝；先消除真实回边，再替换服务定位器，避免只把隐式循环换成显式循环。

## 任务与约束

1. 比较缓存实现直接使用 Mapper、失效参数携带用户名两种方案的行为和代价，选择一种实现并画出修改前后的依赖关系。
2. 检查所有调用方，移除管理员服务接口的 `getCacheService()`，以构造注入替代 `SpringUtil` 定位。
3. 保持缓存 key、失效时机和公开 HTTP 路径；不顺便修复原有缓存行为。
4. 编写特征测试，覆盖改密、更新、删除及管理员不存在时的缓存失效；检查 Redis 调用与用户名的数据来源。在实际 Spring 容器中验证相关服务可以装配。

## 验收依据

- 同一组特征输入在修改前后结果一致，注明测试使用的替身与实际运行范围。
- 生产代码中管理员服务的 `SpringUtil` 使用从 1 处减少到 0；缓存实现不再反向依赖管理员服务。
- 说明两种设计的代价、依赖图，以及旧版依赖服务定位器带来的测试接缝问题。
- 注意原有删除行为：删除数据库记录后再查询用户名；若记录不存在，则不删除管理员缓存，仍删除资源缓存。用户名被更新时，失效查询取得的是更新后的用户名。这些行为本题保留，并非宣称其符合所有业务预期。

## 参考实现与证据

参考分支 `exp/e4-08`，说明、测试与完整日志见 `SDC/E4/answers/E4-08/`。采用 Mapper 直接查询，管理员服务构造注入缓存服务；同一套测试修改前、修改后均 4/4 通过，含最小 Spring 容器装配。Mapper 和 Redis 使用 Mockito 替身，未验证真实数据库与 Redis。

本次实现基于 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`；题目原有 `baseline-dcaa93b3` 是历史 Issue 基线，并非本次比较的 base。JDK 17 下 test 阶段编译成功；额外 package 两次均在 Docker 镜像阶段因环境权限失败，不声称打包成功。
