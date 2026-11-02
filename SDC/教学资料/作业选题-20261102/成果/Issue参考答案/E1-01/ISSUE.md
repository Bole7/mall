# [E1-01] 会员领取优惠券：调用链与数据流观察

## 目标

沿会员领券入口追踪当前会员身份、领券资格检查、历史写入、券数量更新及历史查询。说明手写服务与生成 Mapper 的边界，并标出两次写入和事务声明位置。

## 范围

- 只覆盖一次领券和 `listHistory` 查询，不延伸到订单结算。
- 不修改产品代码；可增加本题观察测试及说明材料。
- 不把 Mock 结果作为数据库或事务回滚证据。

## 验收

- [ ] 标出 `/member/coupon/add/{couponId}` 与 `/member/coupon/listHistory` 控制器入口及服务调用链。
- [ ] 说明当前会员来源、优惠券数量／领取时间／限领次数检查。
- [ ] 观察一次成功路径中的历史插入和券数量更新，列出两者各自改变的数据。
- [ ] 说明 `add` 接口方法上的 `@Transactional`；区分源码声明与运行时事务验证。
- [ ] 说明 `listHistory` 按当前会员筛选，并可附加 `useStatus` 条件。
- [ ] 提供一项本地可重复运行的观察／断言和实际命令结果；注明 Mock 未覆盖数据库与事务。

## 参考基线

`7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。参考答案见同目录 `README.md`，本轮测试日志见 `evidence/test.log`。


## 已发布的参考代码

- [完整参考答案、运行说明与证据](https://github.com/meng004/mall/tree/exp/e1-01/SDC/answers/E1-01)
- [独立参考分支](https://github.com/meng004/mall/tree/exp/e1-01)
- 用途：学生学习参考；不创建 PR，不合入 mall 主线。

## 学生复现入口

```sh
git clone --branch exp/e1-01 --single-branch https://github.com/meng004/mall.git mall-e1-01
cd mall-e1-01
mvn -pl mall-portal -am -Dtest=SDCE101ReferenceTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

JDK 17+；本轮使用 JDK 26.0.1、Maven 3.9.16 和已有离线缓存。1项测试通过，0失败、0错误、0跳过。首次运行须能下载依赖，不能直接照抄离线 `-o`。参考基线为 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。
