# 验证首页品牌新增的 AOP 事务代理

## 背景

`SmsHomeBrandService#create` 声明了 `@Transactional`，实现类逐条调用 `SmsHomeBrandMapper.insert`。仅看到注解无法说明运行时调用是否经过 Spring 代理，也不能据此推断异常时数据库已回滚。

## 目标

在 E4 测试模块加入最小机制验证，观察 Spring 容器代理的事务动作，并对照直接构造对象调用。

## 验收标准

- 测试确认容器取得的 `SmsHomeBrandService` 是 AOP 代理。
- 正常新增时 mapper 写入两次，事务管理器收到提交且未收到回滚。
- 第二次 mapper 写入抛出运行时异常时，异常向调用方传播，事务管理器收到回滚且未收到提交。
- 直接构造实现类并注入 mapper 后调用新增，不与事务管理器交互。
- 变更仅限 E4 的 test-scope 依赖和测试文件，不修改业务实现、事务传播级别、回滚规则或写入顺序。

## 验证边界

Mapper 与事务管理器均为替身；结果只证明代理触发了对应事务动作，不证明真实数据库状态回滚。自调用示例是测试专用子类夹具，不表示生产实现存在自调用问题。


## 已发布的参考代码

- [完整参考答案、运行说明与证据](https://github.com/meng004/mall/tree/exp/e4-12/SDC/answers/E4-12)
- [独立参考分支](https://github.com/meng004/mall/tree/exp/e4-12)
- 用途：学生学习参考；不创建 PR，不合入 mall 主线。

## 学生复现入口

```sh
git clone --branch exp/e4-12 --single-branch https://github.com/meng004/mall.git mall-e4-12
cd mall-e4-12
mvn -pl SDC/E4 -am -Dtest=BrandTransactionTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

JDK 17+；本轮使用 JDK 26.0.1、Maven 3.9.16 和已有离线缓存。2项测试通过，0失败、0错误、0跳过。首次运行须能下载依赖，不能直接照抄离线 `-o`。参考基线为 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。
