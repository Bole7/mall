# Issue #46 更新草稿

商品下架写入点现在通过 `PmsProductUnlistingPort` 发布一次内存通知，admin 不直接依赖搜索模块。实现只覆盖 `publishStatus == 0` 且 mapper 更新行数大于 0 的路径；通知异常记告警但不改变既有更新结果，数据库与外部系统之间仍不是原子事务。

本答案以源码提交 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8` 为实现起点；Issue 标记基线为 `baseline-dcaa93b3`。本答案未接入 ES、消息中间件或其他商品写入点，内存适配器仅用于教学验证。
