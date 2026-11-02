# E1-02 会员收货地址

范围：基线源码的读取路径与默认地址更新路径观察。生产代码未修改。

## 路径

```text
读取：Controller GET /member/address/list 或 GET /member/address/{id}
  -> UmsMemberReceiveAddressServiceImpl.list/getItem
  -> UmsMemberService.getCurrentMember() 得到身份
  -> Example 条件 member_id = 当前会员；详情再加 id = 请求路径参数
  -> Mapper selectByExample -> ums_member_receive_address

写入：Controller POST /member/address/update/{id}
  -> Service.update(id, 请求体)
  -> 先清除：UPDATE 默认标记为 0，WHERE member_id = 当前会员 AND default_status = 1
  -> 再设置：更新请求地址，WHERE member_id = 当前会员 AND id = 路径 id
  -> Mapper updateByExampleSelective -> ums_member_receive_address
```

会员身份来自认证上下文服务，不由请求体提供；请求体承载地址字段，路径参数提供目标地址 id。查询/更新条件由服务端组装，会员过滤发生在 Example 创建处。

当请求 `defaultStatus=1` 时，服务先清当前会员的旧默认地址，再更新指定地址。请求未提供默认状态时服务补为 0。接口声明了 `@Transactional`，但本观察测试只验证服务调用 mapper 的条件、字段与顺序；Mockito 不验证 Spring 事务代理、数据库原子性或真实 SQL 行为。

## 生成代码边界

`UmsMemberReceiveAddressMapper`、`UmsMemberReceiveAddressExample` 和对应 mapper XML 属于 MyBatis Generator 产物；业务身份获取、条件组装、默认状态处理和调用顺序位于 portal Service。生成模型/Example 用于表达持久层结构，不应把业务授权规则误读成模型自身保证。

## 证据

- `UmsMemberReceiveAddressObservationTest` 实际调用 Service，验证 list 的会员条件、默认地址两次 mapper 调用的先后、更新字段及目标会员/id 条件。
- 新鲜测试运行命令和计数记录在 `run.log`。
- 测试使用仓库已有的 `mock-maker-subclass` Mockito 模式，避免 JDK 26 inline mock maker 自附加限制。
- 证据不覆盖其他 CRUD 方法、认证链、事务代理、MyBatis SQL 执行或数据库并发。
