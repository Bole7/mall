# E1-03 商品收藏：Issue 草稿

## 标题

导读商品收藏 add/list/detail：核对 sqlEnable 分支、会员隔离与商品快照来源

## 目标

阅读并核实 `mall-portal` 商品收藏链路，说明 MySQL 商品表与 Mongo 收藏文档各自承担的职责，并区分源码推断与 mock 测试能够证明的范围。

## 范围

- `MemberProductCollectionController` 的 `/add`、`/list`、`/detail`。
- `MemberCollectionServiceImpl.add/list/detail`。
- `PmsProductMapper.selectByPrimaryKey`、`MemberProductCollectionRepository` 的调用条件。
- `mongo.insert.sqlEnable` 为 true/false 时的两条路径。

不要求安装 MongoDB，不修改业务产品代码，不延伸 delete/clear、订单结算、搜索同步或并发唯一性。

## 任务

1. 画出两种 `sqlEnable` 配置下 add 的数据流。
2. 说明当前会员信息从哪里来，并指出请求体中的 `memberId/memberNickname/memberIcon` 是否作为身份依据。
3. 说明重复收藏的判定条件，以及 list/detail 的查询条件和分页换算。
4. 标出商品快照字段 `productName/productSubTitle/productPrice/productPic` 在两种配置下的来源。
5. 编写最小 Mockito 测试，调用真实 `MemberCollectionServiceImpl`，覆盖有效新增、重复、缺失/已删除商品、两种配置和当前会员查询条件。
6. 记录未被测试证明的数据库、事务、索引、并发和 HTTP 边界。

## 验收

- 代码引用包含文件路径、方法和行号；
- true 分支验证 MySQL 查询、删除状态判断、快照复制和 Mongo save；
- false 分支验证不调用 MySQL 且保留请求快照；
- 重复收藏验证不 save 且不再查商品；
- list/detail 验证使用当前会员 ID；
- 测试结果如实记录，不能把 Mockito 交互测试写成 Mongo/MySQL 或端到端证据。

## 参考实现边界

参考测试位于 `SDC/E1/src/test/java/com/macro/mall/sdc/e1/MemberCollectionServiceTest.java`。本草稿不要求修复产品代码；若学生提出 Mongo 唯一索引、事务或并发修复，需另列需求、失败场景和真实数据库证据。

