# E1-05 品牌改名：追踪名称传播

## 任务

基于 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`，从后台品牌更新入口追踪品牌名称如何传播到商品。范围限于品牌和商品，不修改业务代码或搜索引擎。

## 交付与验收

- 绘制带类名的调用或数据流图，说明品牌和商品表的字段映射、更新条件及调用顺序。
- 解释 ID 关联和商品名称副本的作用，以及为什么不能只检查品牌表。
- 在本人环境完成构建、课程只读查询，并对一项具体推断受控核实；记录运行前预期、实际观察、修正与证据。可用 Mock 观察服务调用，须说明其不等于数据库或事务验证。
- 提交一页说明及不超过两分钟的本人录屏，将代码与证据链接并入同一作品。

入口提示：`PmsBrandController`、`PmsBrandServiceImpl.updateBrand`，以及相关生成 Mapper 和模型。


## 已发布学习参考答案

[答案、代码依据和完整验证日志](https://github.com/meng004/mall/tree/exp/e1-05/SDC/E1/answers/E1-05)；[独立参考分支](https://github.com/meng004/mall/tree/exp/e1-05)。

```sh
git clone --branch exp/e1-05 --single-branch https://github.com/meng004/mall.git mall-e1-05
cd mall-e1-05
mvn -pl mall-admin -am -DskipTests=false -Dmaven.compiler.proc=full -Dtest=E105BrandUpdateTest -Dsurefire.failIfNoSpecifiedTests=false test
```

JDK17定向测试1项通过，0失败/错误/跳过。MockMaker配置只用于此教学分支的定向测试；不证明真实SQL/事务/HTTP链路。不创建PR，不合入mall主线。
