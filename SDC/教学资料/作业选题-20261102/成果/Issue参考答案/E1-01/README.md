# E1-01 会员领取优惠券：参考答案

## 范围

本材料基于 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`，只分析一次会员领券及领券历史查询，不修改产品代码，也不延伸到订单结算。对应候选条目要求读取当前会员、检查券数量／领取时间／限领次数、写领取历史并更新券数量；最低产物是调用与数据流图及两次写操作的边界说明。

## 调用与数据流

```text
POST /member/coupon/add/{couponId}
  UmsMemberCouponController.add(couponId)
    -> UmsMemberCouponService.add(couponId) [接口方法有 @Transactional]
      -> UmsMemberCouponServiceImpl.add
        -> memberService.getCurrentMember()
        -> couponMapper.selectByPrimaryKey(couponId)
        -> couponHistoryMapper.countByExample(couponId, currentMember.id)
        -> couponHistoryMapper.insert(SmsCouponHistory)
        -> couponMapper.updateByPrimaryKey(SmsCoupon)

GET /member/coupon/listHistory?useStatus=...
  UmsMemberCouponController.listHistory(useStatus)
    -> UmsMemberCouponService.listHistory(useStatus)
      -> UmsMemberCouponServiceImpl.listHistory
        -> memberService.getCurrentMember()
        -> couponHistoryMapper.selectByExample(memberId[, useStatus])
```

实现位置和本基线行号：控制器入口见 `mall-portal/src/main/java/com/macro/mall/portal/controller/UmsMemberCouponController.java:37`、`:45`；接口及 `add` 的事务声明见 `mall-portal/src/main/java/com/macro/mall/portal/service/UmsMemberCouponService.java:15`；服务读取会员、券和资格条件见 `mall-portal/src/main/java/com/macro/mall/portal/service/impl/UmsMemberCouponServiceImpl.java:43`；历史对象构造与插入见同文件 `:64`；券库存及领取数更新见 `:76`；历史查询按当前会员及可选使用状态过滤见 `:102`。

## 两次写入与事务边界

一次成功领取依次调用 `SmsCouponHistoryMapper.insert`（新建一条会员券历史，包含会员 ID、券 ID、券码、昵称、领取时间、主动领取类型和未使用状态）和 `SmsCouponMapper.updateByPrimaryKey`（券剩余 `count` 减一、`receiveCount` 加一）。对应实现是 `UmsMemberCouponServiceImpl.java:75`、`:79`；Mapper 是生成接口，SQL 分别位于 `mall-mbg/src/main/resources/com/macro/mall/mapper/SmsCouponHistoryMapper.xml:109`、`mall-mbg/src/main/resources/com/macro/mall/mapper/SmsCouponMapper.xml:394`。生成 Mapper/XML 属于数据访问边界，不是本题手写业务入口。

`add` 的 `@Transactional` 写在 `UmsMemberCouponService` 接口方法上（基线 `:19`），实现类方法没有重复注解。结合 Spring 的注解事务机制，这为代理调用的事务边界提供源码证据；本材料的 Mockito 测试只观察 Java 调用和参数，不经过 Spring 事务代理或数据库，**不能证明两次 SQL 在运行时处于同一事务，也不能证明失败回滚**。要确认运行事务行为，需另做 Spring 容器／真实事务管理器集成验证，并在事务中触发第二次写失败后检查数据库状态。

## 已运行的观察

`mall-portal/src/test/java/com/macro/mall/portal/sdc/SDCE101ReferenceTest.java` 使用已有 JUnit 5、Mockito 和 Spring Test 依赖创建服务实例。它断言当前会员 101 的领券历史先于券行更新；历史包含会员、券、领取状态与时间；券数量由 2 到 1、领取数量由 4 到 5；`listHistory(0)` 的查询条件限定会员 101 和未使用状态；并检查接口方法声明的事务注解。

运行结果和完整 Maven 摘要见 `evidence/test.log`。这是 mock 单元观察，不是 HTTP、数据库或事务回滚验证。

## 证据边界

源码事实：入口路由、会员 ID 来源、资格检查条件、两次 mapper 调用顺序、历史查询过滤条件以及接口方法上的事务注解，均由本基线源码支撑。运行事实：上述 mock 测试本轮成功执行并检查了捕获到的调用／查询条件。未验证：Spring 事务代理是否在真实配置下生效、数据库提交／回滚、并发领券下限量是否严格，以及完整 HTTP 链路。

候选库最低证据要求已覆盖调用链、一次模拟成功领取、历史查询、两次写入及事务声明边界。这里没有将注解或 mock 调用描述为实际事务执行证据。
