# E2-09 商品下架通知参考答案

## 规格与小 ADR

下架写入点是 `PmsProductServiceImpl.updatePublishStatus`。当 mapper 更新返回行数大于 0 且 `publishStatus == 0` 时，服务通过 `PmsProductUnlistingPort` 通知请求中的商品 ID；上架、零行和写入异常均不通知。通知异常记录告警并吞掉，原更新行数仍返回，以遵守“不改下架现有数据库行为”；这不证明索引已同步。若批量请求中含未命中 ID，端口仍收到完整请求 ID 列表，适配器需容忍重复或不存在的 ID。

一致性等级为“成功写入后的尽力通知，失败不影响原写入结果”：本方法没有声明式事务；通知失败只记日志，不能据此断言数据库回滚或已跨系统提交。本实现不声称跨系统原子性，也不连接 ES 或消息中间件。admin 只依赖端口，内存适配器用于教学验证，未来可替换为搜索适配器。

否决方案：直接依赖 `mall-search` 会倒置部署边界；引入消息/通用事件框架超出单一写入点；通知失败后抛异常会改变现有写入结果，补偿回滚则错误宣称跨系统原子性。

其他商品更新、品牌/分类冗余字段更新暂不接入，因为本案例只固定下架这一写入点。

## 基线与失败/成功证据

线上 Issue #46 的候选基线标记为 `baseline-dcaa93b3`；本次隔离参考实现 checkout 的实际源码提交为 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。两者不是同一个基线。

- 改前：`PmsProductServiceImpl.updatePublishStatus` 只调用 mapper，测试中的通知端口没有调用。
- 改后：成功下架恰好通知一次；数据库异常、零行、非下架状态不通知；通知失败记告警但仍返回 mapper 更新行数。

## 运行

```text
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  mvn -pl SDC/E2 -am -DskipTests=false -Dtest=ProductUnlistingNotificationTest -Dsurefire.failIfNoSpecifiedTests=false test
```

教学内存适配器只保留最近一批 ID，不累计历史通知；本分支 SDC/E2 测试资源使用 subclass MockMaker，以避开本机 inline agent 自附加失败；这影响本模块 final/static 对象的模拟能力，本轮只验证本题定向测试。
