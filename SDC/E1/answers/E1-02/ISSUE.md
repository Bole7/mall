# E1-02 会员收货地址

## 目标

基于会员收货地址服务，解释当前会员身份来源、请求字段与服务端查询条件的区别，并追踪默认地址更新的两步顺序及生成代码边界。

## 验收标准

- 画出一条读取路径和一条更新路径。
- 标出会员过滤条件如何由当前登录会员生成。
- 说明设为默认地址时先清除当前会员旧默认地址，再更新目标地址。
- 区分 portal 业务逻辑与 MyBatis Generator 生成的 Mapper、Example、XML。
- 提供实际服务调用的 Mockito 观察测试；明确其不证明数据库事务或真实 SQL 行为。

## 学习材料

`SDC/E1/answers/E1-02/README.md` 与 `UmsMemberReceiveAddressObservationTest`。

## 状态

已发布学习参考答案，验证范围见下文。


## 已发布学习参考答案

- [答案说明、源码依据和验证记录](https://github.com/meng004/mall/tree/exp/e1-02/SDC/E1/answers/E1-02)
- [独立参考分支](https://github.com/meng004/mall/tree/exp/e1-02)
- 基线：`7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`；不创建PR，不合入mall主线。
- JDK 26 定向验证：1项通过，0失败/错误/跳过。仅证明源码和Mock调用，不证明真实DB/事务/HTTP链路。

```sh
git clone --branch exp/e1-02 --single-branch https://github.com/meng004/mall.git mall-e1-02
cd mall-e1-02
mvn -pl mall-portal -am -Dtest=UmsMemberReceiveAddressObservationTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```
