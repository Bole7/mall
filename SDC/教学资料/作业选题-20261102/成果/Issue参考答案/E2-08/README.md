# E2-08 商品前台可见规则参考答案

参考实现基线为 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`；Issue 原始任务记录的历史基线为 `baseline-dcaa93b3`，两者不是同一个基线。本示例针对前者，独立教学分支不合并。

`ProductVisibilityCriteria.apply` 让 portal 搜索、品牌商品列表和首页推荐商品共用 `delete_status = 0`、`publish_status = 1`。首页新品和人气推荐两条 SQL 加删除过滤是单独的行为修正：已上架但已删除的商品不再返回。商品详情仍按主键读取；search 未改，审核状态不构成当前规则。

开发视图：`portal service -> portal visibility criteria -> PmsProductExample -> database`；首页 `HomeDao -> HomeDao.xml -> database`，`search -> EsProductDao.xml -> database / search index` 独立部署。当前 helper 只消除三个 Java 调用点的重复，不能被 SQL 或独立 search 直接复用。若将来明确要求审核未通过不可见，三个 Java 条件由 3 处降到 helper 1 处；首页 SQL 两条及 search 导入 1 处仍需分别处理，共由 6 处变为 4 处。详情是否应受限需要独立需求决定，不计入上述列表查询改动数。

## 复现

需要 JDK 17、Maven 及 H2 2.1.214 测试依赖。项目默认跳过测试，必须显式开启：

```sh
mvn -pl mall-portal -am -DskipTests=false -Dtest=ProductVisibilityRulesTest,HomeProductVisibilitySqlTest -Dsurefire.failIfNoSpecifiedTests=false clean test
```

基线对照：在基线检出中仅复制本分支的两个测试文件及 `mall-portal/pom.xml` 的 H2 test 依赖。添加 `-De2.characterizeBaseline=true` 运行以上命令，旧行为特征测试通过，首页两查询各返回商品 `[1, 2]`；去掉此参数重新执行，新期望失败，两条查询均实际返回 `[1, 2]` 而期望 `[1]`。改后默认测试通过。证据见 `evidence/` 三份完整日志。本轮执行使用本地离线缓存 `-o -Dmaven.repo.local=/private/tmp/m2`，日志记录具体命令环境；其他机器可联网解析依赖。

服务测试直接调用三个服务，捕获各自传给 Mapper 的查询条件；它验证两项可见条件存在，并不证明所有商品结果、分页或所有可选筛选组合完全一致。SQL 测试用 MyBatis 读取真实 XML 并在 H2 MySQL 模式执行；没有测试真实 MySQL、HTTP、搜索索引或详情。测试没有把源码字符串当作 SQL 执行。
