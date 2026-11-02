本题独立参考分支：`refs/heads/exp/e4-11`。仅作学习参考，不向 mall 主线创建或合并 PR。

# E4-11 参考答案：退货详情关联映射

本目录是可供学生访问的学习参考入口，包含操作说明、完整测试补丁、Issue 说明和本轮测试日志。建议在课程仓库 `SDC/E4` 应用 `E4-11.patch`；学生可从 `ReturnApplyMappingTest` 阅读入口开始，再对照真实 XML、Mapper 接口和 DTO。补丁只为测试模块增加 `mall-admin` 与 H2 的 test 依赖及映射测试，不改业务 SQL、生成 Mapper 或业务表结构。

## 基线与验证

- 基线：`7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。
- 独立副本：`/private/tmp/mall-e4-11`，测试后恢复为只含交付改动的差异；原始工作区没有改动。
- 命令：`mvn -o -pl SDC/E4 -am -Dtest=ReturnApplyMappingTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test`。
- 环境：macOS arm64、JDK 26.0.1、Maven 3.9.16；离线模式使用本机已有依赖缓存。因缓存仓库 ID 限制，验证时临时在副本 POM 声明了缓存记录的 `huaweicloud` 仓库 ID；该本机专用声明不在交付补丁中。
- 结果：`BUILD SUCCESS`；1 项测试通过，失败、错误、跳过均为 0。完整本轮日志见 `fresh-test.log`。

## 学习要点

`OmsOrderReturnApplyDao#getDetail(@Param("id") Long id)` 将单一参数命名为 `id`；XML 的 `WHERE ra.id=#{id}` 使用相同名称。查询选择 `ra.*`，主对象由继承的 `OmsOrderReturnApplyMapper.BaseResultMap` 映射。地址列使用 `ca_` 别名，`<association property="companyAddress" ... columnPrefix="ca_"/>` 将其交给 `OmsCompanyAddressMapper.BaseResultMap`，关联主键是 `ca_id`，不是申请记录的 `id`。

测试由 MyBatis 读取项目中的两个生成 Mapper XML 和真实 `dao/OmsOrderReturnApplyDao.xml`，创建 Mapper 代理并查询 H2 MySQL 兼容模式内存库。它断言正常关联中的申请 ID、地址 ID 和多个地址字段；第二个独立申请指向不同地址，验证列前缀/关联值不会串行或误用主键；无关联地址时属性为 null；申请 ID 未命中时结果为 null。

## 边界

本次是 H2 内存数据库验证，不是生产 MySQL 验证；也不覆盖完整 Spring 应用、真实数据、事务或所有数据库方言行为。无需为了让参考测试通过修改正确的业务 SQL。参考答案随独立分支提供，不创建 PR，也不合入 mall 主线。
