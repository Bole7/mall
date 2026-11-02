## 靶点
问题：后台与搜索是两个部署单元，中间没有任何接缝。
- `mall-admin` 与 `mall-portal` 中 0 处引用搜索模块；
- 下架 `PmsProductServiceImpl.updatePublishStatus`（256–262 行）只改 MySQL；
- 品牌改名 `PmsBrandServiceImpl.java` 55–60 行、分类改名 `PmsProductCategoryServiceImpl.java` 75–80 行只改商品表里的冗余名；
- 搜索只有手动入口：`EsProductController` 的导入、按 id 创建、删除、批量删除。

原则：架构层面的一致性与依赖方向；ADR（一致性等级、由谁触发同步、被否决方案）；依赖倒置：admin 定义“商品变更”端口，不直接依赖搜索模块。

## 验收准则
- ADR 一份，并入一页说明
- 下架一个写入点经端口发布一次变更；测试用内存假实现断言，改前失败、改后通过
- 说明其余写入点为何暂不接

## 不可变约束
- 不接入 ES 或消息中间件，不改搜索模块
- 不改下架的现有数据库行为

## 状态
已发布独立学习参考实现（进阶），不创建 PR、不合入主线。历史题目标记 `baseline-dcaa93b3`；本答案实际起点为 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。

## 参考答案与复现

[参考答案及测试日志](https://github.com/meng004/mall/tree/exp/e2-09/SDC/E2/answers/E2-09)；[参考分支](https://github.com/meng004/mall/tree/exp/e2-09)。

```sh
git clone --branch exp/e2-09 --single-branch https://github.com/meng004/mall.git mall-e2-09
cd mall-e2-09
mvn -pl SDC/E2 -am -DskipTests=false -Dtest=ProductUnlistingNotificationTest -Dsurefire.failIfNoSpecifiedTests=false test
```

JDK 17，5 项定向测试通过，失败/错误/跳过均为 0。成功写入且 publishStatus 为 0 时调用一次端口；零行、写入异常、非下架不通知；通知异常只记录告警，保持原写入返回值。端口收到完整请求 ID，可能包含不存在的 ID；适配器须容忍。内存教学适配器只记录最近一批，不累计历史数据。

只验证真实服务与替身 Mapper/端口；没有接入 ES、消息中间件或真实数据库，不表示搜索索引已经同步，不保证跨系统原子性。SDC/E2 的 subclass MockMaker 配置影响本分支该模块 final/static 模拟能力，未验证全模块测试。
