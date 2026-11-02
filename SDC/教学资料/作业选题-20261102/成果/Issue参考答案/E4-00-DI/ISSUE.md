# E4-00 DI 教师示例：促销服务改为构造注入

## 背景

`OmsPromotionServiceImpl` 通过字段注入 `PortalProductDao`。现有促销特征测试先无参创建服务，再通过反射设置私有字段，测试必须了解实现细节，也无法验证 Spring 对目标构造器的装配。

## 目标

仅将该依赖改为构造器注入；保留促销业务行为、事务声明和事务边界。保持原七组业务输入与断言，并用最小 Spring 容器验证服务可被装配且依赖可用。

## 学生任务

1. 以基线 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8` 为起点，阅读服务、`PortalProductDao` 和 `SDC/E4` 的 `PromotionTest`，查找直接构造服务的调用方。
2. 在改造前运行 `mvn -pl SDC/E4 -am test` 并保存结果；保留原测试的七组输入和断言。
3. 将该 DAO 改为构造器注入，同步修改所有直接构造调用，以构造器传入测试替身。不要修改促销逻辑、事务声明或边界。
4. 编写最小 Spring 容器测试，注册 DAO 替身和服务类，验证容器创建服务并使用注入的 DAO；再次运行相同测试命令并保存结果。
5. 说明构造注入的收益与调用方修改成本，以及它不自动保证非 null，也不自动构成依赖倒置；说明测试证据的范围。

## 验收

- 促销服务使用 `PortalProductDao` 单构造器注入，无字段注入。
- 原 `PromotionTest` 保持一个测试、七组输入，替身通过构造器提供，不再反射设置字段。
- 最小 Spring 容器测试成功创建服务并验证 DAO 调用。
- `mvn -pl SDC/E4 -am test` 在 JDK 17 下通过。
- 不改促销规则、事务注解或事务边界；不将构造注入表述为依赖倒置或非 null 保证。

## 参考答案与复现

- [参考分支](https://github.com/meng004/mall/tree/exp/e4-00-di)；[答案、补丁和完整日志](https://github.com/meng004/mall/tree/exp/e4-00-di/SDC/E4/answers/E4-00-DI)。此分支供学习参考，不创建 PR、不合入主线。
- 基线 JDK 17 实测 1 项测试通过；改后 2 项测试通过，均为失败、错误、跳过各 0。原有 `PromotionTest` 是 1 项测试、7 组输入，并非 7 项测试。

```sh
git clone --branch exp/e4-00-di --single-branch https://github.com/meng004/mall.git mall-e4-00-di
cd mall-e4-00-di
mvn -pl SDC/E4 -am test
```

使用 JDK 17；首次运行需要 Maven 依赖可用。参考分支是改后状态，改前结果和源码对照以基线及答案中的日志、补丁为准。

## 范围说明

最小容器仅显式注册服务类和 DAO 替身，验证单构造器装配与 DAO 调用；不启动完整 Spring Boot 应用，不验证组件扫描、生产数据库、真实 MyBatis 或端到端行为。七组输入只证明所测行为，不代表所有促销场景。构造器不自行拒绝 null。

这是新的 E4-00 DI 示例，不复用 Issue #20（优惠券策略重构）或 `exp/e4-00`。
