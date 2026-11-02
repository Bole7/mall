# E4-11：验证退货申请详情的 MyBatis 关联映射

**状态：参考答案；非 PR。** 参考代码仅供学习，未合入 mall 主线。

**基线版本：** `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`

## 目标

用隔离数据库和项目实际 MyBatis XML 验证 `OmsOrderReturnApplyDao#getDetail` 的参数绑定、主对象映射及公司地址关联映射。无需修改正确的业务 SQL、生成 Mapper 或业务表结构。

## 检查内容

- `@Param("id")` 与 SQL `#{id}` 一致，且申请 ID 与地址 ID 分别断言。
- 正常关联下，`ca_` 前缀把地址列映射到 `companyAddress`，验证地址名、联系人和城市。
- 第二个申请映射至另一地址，作为独立额外输入核验关联行与字段映射。
- 左连接无地址时关联属性为 null，查询 ID 不存在时结果为 null。

## 验证记录

基线副本中使用 JDK 26.0.1、Maven 3.9.16 执行指定 Maven 测试，结果为 1 项通过、0 失败、0 错误、0 跳过。完整日志随参考答案保存。离线运行使用本机依赖缓存；详见 README 的环境说明。

## 限制

数据库为 H2 `MODE=MySQL` 内存库。该结果只说明所列 SQL、参数绑定和结果映射在本次 H2 环境通过，不等于生产 MySQL 验证，也不证明完整应用、真实事务或其他方言行为。


## 已发布的参考代码

- [完整参考答案、运行说明与证据](https://github.com/meng004/mall/tree/exp/e4-11/SDC/answers/E4-11)
- [独立参考分支](https://github.com/meng004/mall/tree/exp/e4-11)
- 用途：学生学习参考；不创建 PR，不合入 mall 主线。

## 学生复现入口

```sh
git clone --branch exp/e4-11 --single-branch https://github.com/meng004/mall.git mall-e4-11
cd mall-e4-11
mvn -pl SDC/E4 -am -Dtest=ReturnApplyMappingTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

JDK 17+；本轮使用 JDK 26.0.1、Maven 3.9.16 和已有离线缓存。1项测试通过，0失败、0错误、0跳过。首次运行须能下载依赖，不能直接照抄离线 `-o`。参考基线为 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。
