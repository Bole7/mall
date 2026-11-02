# E1-04 浏览记录行为分析

## 目标

依据真实服务代码分析浏览记录的 create/list/clear 数据流，解释会员数据归属、分页参数、时间倒序和清空范围；再与商品收藏对照事件记录和当前状态，判断重复浏览行为。

## 学习者任务

1. 画出 create、list、clear 的调用或数据流，并标记当前会员信息的来源。
2. 说明 create 如何处理请求中的会员 ID、记录 ID、创建时间和商品 ID；分别说明 `mongo.insert.sqlEnable` 开关两侧的商品快照来源与校验。
3. 追踪 `pageNum=1,pageSize=5` 如何传到 Spring Data，并说明列表的会员过滤、时间顺序和 clear 的删除条件。
4. 对照 E1-03 收藏，至少指出两项经代码支持的行为差异；说明相同商品重复浏览是否会追加记录，并提供方法依据。
5. 区分单元/Mock 证据和真实 Mongo、SQL 集成证据，不把模拟调用描述成数据库执行结果。

## 验收证据

- 对应方法及字段的源码引用。
- 至少两项收藏与浏览记录的实际差异。
- 对重复浏览语义的明确结论与调用证据。
- 通过 Mockito 对真实 service 实现的测试，验证当前会员、时间/ID、分页/排序调用、会员清空条件及保存次数。
- 说明未启动 Mongo/SQL 时结论的边界。

## 非目标

不修改 mall 产品代码，不实现浏览历史功能，不将结果扩展到批量删除、清理策略或端到端数据库验证。


## 已发布学习参考答案

- [答案说明、源码依据和验证记录](https://github.com/meng004/mall/tree/exp/e1-04/SDC/E1/answers/E1-04)
- [独立参考分支](https://github.com/meng004/mall/tree/exp/e1-04)
- 基线：`7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`；不创建PR，不合入mall主线。
- JDK 26 定向验证：8（新增5、已有3）项通过，0失败/错误/跳过。仅证明源码和Mock调用，不证明真实DB/事务/HTTP链路。

```sh
git clone --branch exp/e1-04 --single-branch https://github.com/meng004/mall.git mall-e1-04
cd mall-e1-04
mvn -pl SDC/E1 -am -Dtest=MemberReadHistoryBehaviorTest,MallFactsTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```
