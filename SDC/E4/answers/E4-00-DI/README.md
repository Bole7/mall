# E4-00 DI 教师示例参考答案

## 结果

将 `OmsPromotionServiceImpl` 的 `PortalProductDao` 从字段注入改为单构造器注入。构造器只保存传入依赖，没有新增非 null 约束；本改动不改变促销算法、事务注解或事务边界。

## 变更范围

- `mall-portal/.../OmsPromotionServiceImpl.java`：移除字段上的 `@Autowired`，增加 `final` 字段和构造器。
- `SDC/E4/.../PromotionTest.java`：保留一个测试和原有七组输入，直接传入 DAO mock，移除反射字段写入。
- `SDC/E4/.../PromotionWiringTest.java`：使用最小 `AnnotationConfigApplicationContext` 注册 DAO 替身和服务类型，确认 Spring 能创建服务并将 DAO 用于调用。

直接构造调用清单（本仓库 Java 源码）：`PromotionTest.check`。已改为 `new OmsPromotionServiceImpl(dao)`。生产代码由 Spring 创建，没有其他直接 `new` 调用。

## 验证

基线与改后都运行 `mvn -pl SDC/E4 -am test`，使用 JDK 17。基线 1 项测试通过；改后 2 项测试通过（业务特征 1 项、容器装配 1 项），失败/错误/跳过均为 0。完整日志见 `before.log` 和 `after.log`。

七组输入对应单品、阶梯命中、阶梯未命中、满减命中、满减未命中、无促销、未知促销类型；每组核对优惠金额、提示文案、可售库存、积分、成长值和数量。结果仅证明这些输入的行为保持，不代表全部促销行为。

## 解释边界

构造注入让依赖在创建对象时显式提供，并使无参创建不再适用；它本身不等于依赖倒置，也不保证传入值非 null。Spring 当前单构造器可自动注入，无需 `@Autowired`。测试容器显式注册该服务和 DAO 替身，不验证组件扫描，不启动完整应用或数据库，也不验证真实 MyBatis 或端到端行为。

历史 `Issue #20` 与 `exp/e4-00` 是优惠券策略重构，不属于本示例；学生任务见 `ISSUE.md`，答案分支为 `exp/e4-00-di`。
