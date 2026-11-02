# E1-06 首页内容聚合观察任务

## 目标

沿 `HomeController.content()` → `HomeServiceImpl.content()` 追踪六块首页内容，记录每块来源、固定条数／分页边界，并受控核实无当前秒杀活动时的返回结构。

## 范围

只新增观察测试和说明材料，不修改产品代码，不分析支付或订单状态机。

## 验收

- 真实调用 `HomeServiceImpl.content()`。
- 覆盖广告、品牌、秒杀、新品、人气、专题六块来源。
- 断言品牌 `(0,6)`、新品／人气／专题 `(0,4)`。
- 无秒杀活动时断言 `HomeFlashPromotion` 包装对象存在但内部字段为空，且不继续查场次或商品。
- 明确 Mock 服务观察不等于数据库或端到端验证。

## 交付

- 测试：`mall-portal/src/test/java/com/macro/mall/portal/sdc/SDCE106Test.java`
- Mockito subclass 配置：`mall-portal/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
- 答案：`SDC/E1/answers/E1-06/README.md`
- 日志：`SDC/E1/outputs/E1-06/evidence/fresh-targeted-test.log`


## 已发布参考答案

[答案与验证记录](https://github.com/meng004/mall/tree/exp/e1-06/SDC/E1/answers/E1-06)；[参考分支](https://github.com/meng004/mall/tree/exp/e1-06)。

```sh
git clone --branch exp/e1-06 --single-branch https://github.com/meng004/mall.git mall-e1-06
cd mall-e1-06
mvn -pl mall-portal -am -Dtest=SDCE106Test -DskipTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.compiler.proc=full test
```

本轮JDK17，2项测试通过，0失败/错误/跳过；只验证真实服务在Mock依赖下的行为，不证明SQL、DB、HTTP或事务。仅供学习，不创建PR、不合入主线。
