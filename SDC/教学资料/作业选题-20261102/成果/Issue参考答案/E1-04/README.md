# E1-04 浏览记录：教师参考答案

## 范围与结论

只分析门户侧浏览记录的新增、列表和清空：`MemberReadHistoryServiceImpl#create/list/clear`。调用方可以提交商品 ID 和商品快照字段；服务端把当前会员 ID、昵称、头像写回记录，重置客户端 ID，并生成当前时间。只要商品 ID 不为空且可选的商品校验通过，每次调用都会 `save` 一条新记录，没有按会员和商品去重，所以重复浏览会新增事件。

```mermaid
flowchart LR
  A[POST /member/readHistory/create] --> B[Service: 校验 productId]
  B --> C[读取当前会员并覆盖会员快照、ID、时间]
  C --> D{mongo.insert.sqlEnable}
  D -->|true| E[MySQL 查询商品并拒绝不存在或已删除商品]
  D -->|false| F[使用请求中的商品快照]
  E --> G[Mongo Repository save]
  F --> G
  H[GET /member/readHistory/list] --> I[当前会员 + PageRequest(pageNum-1,pageSize)]
  I --> J[按 createTime DESC 分页]
  K[POST /member/readHistory/clear] --> L[按当前会员 ID 删除全部记录]
```

## 代码依据

- [控制器 `MemberReadHistoryController`](https://github.com/meng004/mall/blob/exp/e1-04/mall-portal/src/main/java/com/macro/mall/portal/controller/MemberReadHistoryController.java)：`:29-40` 暴露 create；`:53-58` 暴露无参数 clear；`:62-68` 列表默认 `pageNum=1`、`pageSize=5`。
- [服务 `MemberReadHistoryServiceImpl`](https://github.com/meng004/mall/blob/exp/e1-04/mall-portal/src/main/java/com/macro/mall/portal/service/impl/MemberReadHistoryServiceImpl.java)：`:37-59` 空商品 ID 早退、从 `getCurrentMember()` 覆盖会员信息、清除客户端 ID、`new Date()`、配置开启时查 SQL 商品并排除不存在/已删除商品，最后每次调用 `save`；`:74-78` 页码减一并调用按创建时间倒序的会员查询；`:81-84` 只按当前会员 ID 清空。
- [Repository `MemberReadHistoryRepository`](https://github.com/meng004/mall/blob/exp/e1-04/mall-portal/src/main/java/com/macro/mall/portal/repository/MemberReadHistoryRepository.java)：`:16-21` 定义会员条件分页倒序查询和按会员删除。
- [记录模型 `MemberReadHistory`](https://github.com/meng004/mall/blob/exp/e1-04/mall-portal/src/main/java/com/macro/mall/portal/domain/MemberReadHistory.java)：`:17-33` 包含会员、商品快照与 `createTime`，使用 Mongo `@Document`；会员 ID 与商品 ID 有索引。
- [收藏服务 `MemberCollectionServiceImpl`](https://github.com/meng004/mall/blob/exp/e1-04/mall-portal/src/main/java/com/macro/mall/portal/service/impl/MemberCollectionServiceImpl.java)：`:33-57` 先查当前会员与商品组合，已有收藏时不保存；`:67-71` 收藏列表分页但 Repository 查询未声明创建时间排序。它保存的是一份当前收藏状态，浏览记录是重复可追加的事件，因此两者至少在去重语义和排序语义上不同。

## 行为验证

`MemberReadHistoryBehaviorTest` 通过 Mockito 注入依赖并直接执行真实服务实现，不启动 Spring 容器，也不连接外部数据库。测试检查了当前会员字段覆盖、ID/时间重置、相同商品两次都调用 `save`、空商品 ID 早退、SQL 补全开启时不存在/已删除商品不保存、页码换算与时间倒序 Repository 方法、清空的会员 ID，以及收藏已存在时不保存而浏览仍追加。

测试文件：`SDC/E1/src/test/java/com/macro/mall/sdc/e1/MemberReadHistoryBehaviorTest.java`。真实 Mongo 查询/删除执行、HTTP 序列化和数据库事务不在 Mock 证据范围内。

## 可复现参考答案

基线 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`；独立分支 `exp/e1-04`。

```sh
mvn -pl SDC/E1 -am -Dtest=MemberReadHistoryBehaviorTest,MallFactsTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

本轮 JDK 26 定向测试 8（新增5、已有3） 项通过，0失败、0错误、0跳过。首次运行需要联网下载依赖；无须运行依赖数据库的全部测试。
