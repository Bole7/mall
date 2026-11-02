# E1-05 品牌改名：导读参考答案

基线：`7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。范围是后台品牌名称传播，不涉及搜索索引。

```text
PmsBrandController.update(id, PmsBrandParam)
  -> PmsBrandServiceImpl.updateBrand(id, param)
     -> PmsProductMapper.updateByExampleSelective(product, example)
        product.brandName = param.name; example: brand_id = id
     -> PmsBrandMapper.updateByPrimaryKeySelective(brand)
        brand.id = id; brand.name = param.name
```

| 数据 | 字段 | 来源 / 条件 |
| --- | --- | --- |
| 品牌 `pms_brand` | `id`, `name` | 路径参数 `id`，请求参数 `name`；按品牌主键更新 |
| 商品 `pms_product` | `brand_id`, `brand_name` | `brand_id` 等于同一 `id`；把 `brand_name` 设置为新名称 |

商品以 ID 关联品牌，也保存名称副本；只检查品牌表无法确认商品名称副本是否同步。服务复制品牌参数，设置 ID；首字母为空时取名称首字符。先调用商品更新，再调用品牌更新，返回品牌更新的行数；商品更新的行数未用于返回值。

源码依据：`mall-admin/src/main/java/com/macro/mall/controller/PmsBrandController.java` 的 `update`；`mall-admin/src/main/java/com/macro/mall/service/impl/PmsBrandServiceImpl.java` 的 `updateBrand`；`mall-admin/src/main/java/com/macro/mall/service/PmsBrandService.java` 的事务注解；生成模型与 Mapper 位于 `mall-mbg`。

## 受控核实

预期：品牌 ID 42 改名为 `New Brand` 时，先向商品 Mapper 传入新名称和唯一条件 `brand_id = 42`，再向品牌 Mapper 传入 ID 42 和新名称；服务返回品牌 Mapper 的结果。`mall-admin/src/test/java/com/macro/mall/service/impl/E105BrandUpdateTest.java` 调用真实服务，以 Mock Mapper 捕获参数并验证顺序及返回值。运行结果和命令见交付日志。

观察限于 Java 调用和传参：未连接数据库，未执行真实 SQL，也未经过 Spring 事务代理或验证回滚、HTTP、搜索索引及并发。接口声明了 `@Transactional`，不能仅凭本测试断言事务是否实际生效。学生仍须本人执行构建、课程只读查询和所选推断核实，保留预期、观察和修正。

## 复现与评审

独立参考分支 `exp/e1-05`，JDK17；仓库根目录运行：

```sh
mvn -pl mall-admin -am -DskipTests=false -Dmaven.compiler.proc=full -Dtest=E105BrandUpdateTest -Dsurefire.failIfNoSpecifiedTests=false test
```

本轮主代理复跑1项通过，0失败/错误/跳过，原始日志见 review-test.log。本分支 mall-admin 测试资源使用 subclass MockMaker，以避开本机 inline agent 自附加失败；这影响本模块 final/static 对象的模拟能力，只验证本题定向测试，未执行模块全部测试。不创建PR、不合入mall主线。
