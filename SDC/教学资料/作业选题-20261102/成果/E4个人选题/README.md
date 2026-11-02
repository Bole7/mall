# E4 个人选题任务单

本次从依赖注入（DI）、对象关系映射（ORM）或面向切面编程（AOP）中选一题，完成最小改造或可运行的机制验证。教师课上演示仍采用促销服务构造注入；个人作业不必照搬该对象。E4-08 保留为进阶题，自主题须先确认范围。

## 选题与直接入口

| 编号 | 题目 | 要完成的工作 | 操作与测试文件 |
|---|---|---|---|
| [E4-10／#50](https://github.com/meng004/mall/issues/50) | 商品优选列表的单依赖注入（DI） | 固定列表行为，把字段注入改为构造传入，验证直接构造与最小容器装配 | [操作说明](E4-10验证/README.md)；[改前测试](E4-10验证/CmsPrefrenceAreaInjectionTest.java)；[改后测试](E4-10验证/改后/CmsPrefrenceAreaInjectionTest.java) |
| [E4-11／#51](https://github.com/meng004/mall/issues/51) | 退货详情与公司地址的关联映射验证（ORM） | 用实际 MyBatis XML 和隔离数据库核验参数与关联结果映射 | [操作说明](E4-11验证/README.md)；[映射测试](E4-11验证/ReturnApplyMappingTest.java)；[参考答案](../Issue参考答案/E4-11/README.md) |
| [E4-12／#52](https://github.com/meng004/mall/issues/52) | 首页品牌新增的事务代理验证（AOP） | 对比代理调用与直接调用，观察成功提交与异常回滚动作 | [操作说明](AOP事务代理/README.md)；[代理测试](AOP事务代理/BrandTransactionTest.java)；[参考答案](../Issue参考答案/E4-12/README.md) |
| E4-08 | 管理员服务与缓存服务循环依赖（进阶） | 先识别并消除真实回边，再讨论注入与行为保持 | [Issue #48](https://github.com/meng004/mall/issues/48)；[案例集合](../1-教学文件/教学案例候选.md#进阶与自选) |

教师 DI 示例：[Issue #59](https://github.com/meng004/mall/issues/59)，分支 `exp/e4-00-di`；历史 #20 与 `exp/e4-00` 是券范围题。

## 版本与准备

代码仓库为 `https://github.com/meng004/mall.git`，本轮核验版本为 `7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8`。在个人副本操作，参考仓库只读。已有个人副本时先检查未提交改动，不在有未保存工作的目录直接切换版本。

新建个人副本可运行：

```sh
rtk git clone https://github.com/meng004/mall.git mall-e4
rtk git -C mall-e4 checkout --detach 7832a9f7dcd5ff16b5b3c6c2b0f178a6b8f2dfe8
rtk git -C mall-e4 switch -c e4-personal
rtk mvn -v
```

Maven 使用的 JDK 应为 17 或更高。下列命令在个人 `mall-e4` 根目录运行；首次构建需联网取得依赖。本轮实测使用 JDK 26 和已缓存依赖，因此启用了 `-Dmaven.compiler.proc=full`。不要把编译、依赖下载失败记为业务测试失败。

三题均复用 `SDC/E4` 测试模块，但**只使用本任务单提供的新测试**，不沿用旧 `exp/E4` 的券范围任务、compare脚本或旧手册。按各题操作说明在该模块的 dependencies 中增加一次 `mall-admin` 测试依赖；ORM 题另加 H2。不要重复加入同一依赖。

## E4-10：单依赖构造注入

**代码路径**：`mall-admin/src/main/java/com/macro/mall/service/impl/CmsPrefrenceAreaServiceImpl.java`。依赖为 `CmsPrefrenceAreaMapper`，入口为 `listAll()`，业务调用方为 `CmsPrefrenceAreaController.listAll()`。

1. 阅读服务与接口，找到字段注入及 `selectByExample` 调用。在个人副本搜索该类所有直接构造位置，并记录结果。
2. 安装改前测试，在下面命令下先取得通过结果，保留输入与断言。
3. 仅删除该字段的 `@Autowired`，将字段设为 `final`，增加接收 Mapper 的构造器；业务方法不改。同步全部直接构造位置，测试直接传入替身并移除相应反射设置。
4. 使用改后测试复跑同一命令，比较两份测试：业务输入与断言相同，只有对象装配方式改变。

```sh
rtk mvn -pl SDC/E4 -am -Dtest=CmsPrefrenceAreaInjectionTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

**保持行为**：空列表、多元素列表的内容及顺序不变；传给 Mapper 的 Example 不增加筛选或排序。最小 Spring 容器应能取得服务并调用同一行为。

**预期**：改前和改后各 1 个测试通过，失败、错误与跳过均为 0；一个测试包含多组断言。这里不连接数据库，也不证明完整应用装配正确。

## E4-11：ORM 关联映射

**代码路径**：`mall-admin/src/main/java/com/macro/mall/dao/OmsOrderReturnApplyDao.java`；`mall-admin/src/main/resources/dao/OmsOrderReturnApplyDao.xml`；`mall-admin/src/main/java/com/macro/mall/dto/OmsOrderReturnApplyResult.java`。入口为 `getDetail(id)`，结果映射为 `returnApplyDetailResultMap`。

1. 对照接口方法、XML 查询和 DTO，标出 id 参数绑定、申请字段、地址列别名及 `ca_` 前缀的去向。
2. 运行提供的测试：它在 H2 内存库建立最小测试表，加载项目真实 XML，通过 Mapper 代理查询；没有用预制对象代替查询。
3. 核对申请 id 与地址 id 不混淆，核对地址名称、联系人及城市；查看关联地址缺失、申请不存在时的结果。
4. 本人选择一个额外输入，例如第二个不同地址对应另一申请，补充独立断言，说明它检验哪条映射规则。不得仅把测试改为打印结果。

```sh
rtk mvn -pl SDC/E4 -am -Dtest=ReturnApplyMappingTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

**预期**：参考测试 1 项通过，失败、错误与跳过均为 0；覆盖正常关联、无地址与 id 未命中。保留主对象与关联对象的断言。无需修改业务 SQL、生成的 Mapper 或表结构；测试表仅存在于内存库。

**边界**：H2 的 MySQL 兼容模式不等于生产 MySQL，本题证明所用 SQL、参数绑定和结果映射在该隔离环境下成立；不证明完整业务或真实事务。

## E4-12：AOP 事务代理

**代码路径**：`mall-admin/src/main/java/com/macro/mall/service/SmsHomeBrandService.java` 的 `create` 声明，以及 `mall-admin/src/main/java/com/macro/mall/service/impl/SmsHomeBrandServiceImpl.java` 的实现。接口声明事务，实现逐条调用 `SmsHomeBrandMapper.insert`。

1. 找到事务注解、实现方法和写入调用，画出“容器代理 → 目标服务 → Mapper”的路径。
2. 运行测试，确认容器取得的对象是代理；正常新增时事务管理器收到提交调用。
3. 令第二次 Mapper 插入抛出运行时异常，断言异常传播、事务管理器收到回滚且未收到提交。对照直接构造服务的调用，它不触发事务管理器。
4. 解释测试替身与真实数据库的区别。测试文件中另有自调用教学夹具：它通过测试子类构造对照情境，**不是生产源码存在自调用缺陷的证据**，该部分可用于进一步解释 T4 的代理边界。

```sh
rtk mvn -pl SDC/E4 -am -Dtest=BrandTransactionTest -Dsurefire.failIfNoSpecifiedTests=false -DskipTests=false -Dmaven.compiler.proc=full test
```

**预期**：2 项参考测试通过，失败、错误与跳过均为 0。不能只看方法上的注解就推断代理动作；不改传播级别、回滚规则或业务顺序。

**边界**：使用 Mapper 和事务管理器替身，观察的是框架发出的事务动作；未连接真实数据库，不能写“数据已回滚”。

## 提交与评分

提交一页说明和不超过2分钟本人讲解录屏，链接本人代码、测试输出及差异。说明代码版本、输入、独立期望、实际结果、T4依据、收益与代价、尚未验证的范围。参考脚手架是起点，须能逐项解释并完成本人选定的输入或边界核验；教师运行日志不是个人作业证据。

- **讲清楚30**：机制入口、协作对象和源码位置15；调用或数据路径、必须保持的行为15。
- **有依据30**：依据T4说明机制及适用条件15；比较改造前后或有无机制的路径、收益与代价15。
- **验证过40**：DI同组前后特征测试，或ORM/AOP正常与边界机制测试25；本人核对构造调用、映射条件或代理路径10；框架集成验证与未覆盖范围5。

不要求改前功能失败或强制修改业务代码。发现缺陷另列。完整题面见[新版超星作业](../1-教学文件/超星录入材料-测验题签到题与作业.docx)，范围见[案例集合](../1-教学文件/教学案例候选.md)。

## 本轮核验记录

2026年11月1日，隔离副本中执行以上三题；运行记录保存在[验证记录](验证记录/)。DI改前、改后均通过；ORM执行真实MyBatis XML与H2；AOP执行最小Spring代理测试。参考仓库的产品源码未因本次题目准备而修改。以上结果仅覆盖所列输入和环境，不代表完整应用、生产MySQL、真实数据库回滚或E4-08已验证。

2026年11月2日补充：三项基础题已发布为 #50～#52；E4-08 的 #48 已提供独立参考实现与 4 项测试；教师 DI 示例为 #59（改前 1 项、改后 2 项）。以上均为独立参考分支，不合入 mall 主线。运行命令与实际验证范围以各 Issue 和对应 README 为准。
