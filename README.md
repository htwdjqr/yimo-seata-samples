## 一、背景

[Seata](https://seata.io/zh-cn/) 是一款开源的分布式事务解决方案，致力于提供高性能和简单易用的分布式事务服务。Seata 将为用户提供了 AT、TCC、SAGA 和 XA 事务模式，为用户打造一站式的分布式解决方案。相信很多用户选择使用Seata都是奔着AT模式去的，AT 模式是 Seata 创新的一种非侵入式的分布式事务解决方案。TCC模式也有其优点，性能高，完全不依赖底层数据库，能够实现跨数据库、跨应用资源管理，可以提供给业务方更细粒度的控制。四种模式对比：

|          | XA                             | AT                                   | TCC                          | SAGA                                                         |
| -------- | ------------------------------ | ------------------------------------ | ---------------------------- | ------------------------------------------------------------ |
| 一致性   | 强一致                         | 弱一致                               | 弱一致                       | 最终一致                                                     |
| 隔离性   | 完全隔离                       | 基于全局锁隔离                       | 基于资源预留隔离             | 无隔离                                                       |
| 代码侵入 | 无                             | 无                                   | 有，要编写三个接口           | 有，要编写状态机和补偿业务                                   |
| 性能     | 差                             | 好                                   | 非常好                       | 非常好                                                       |
| 场景     | 对一致性，隔离性有高要求的业务 | 基于关系型数据库；通过JDBC访问数据库 | 对性能有要求；非关系型数据库 | 业务流程长、业务流程多 参与者包含其它公司或遗留系统服务，无法提供 TCC 模式要求的三个接口 |

Seata 支持同一项目中多种模式混合使用的，默认是AT模式，本文主要介绍同一个项目中如何集成AT和TCC两种模式。代码在这：[yimo-seata-samples](https://github.com/htwdjqr/yimo-seata-samples)。

## 二、集成准备

框架以及对应的版本信息如下：

- JDK：1.8
- Spring Boot：2.6.3
- Spring Cloud Alibaba：2021.0.1.0
- Nacos：1.4.2
- Dubbo：2.7.15
- Seata Server：1.7.0
- Seata Client：1.7.0

业务场景参考官方 [快速开始](https://seata.io/zh-cn/docs/user/quickstart) 中的场景，用户购买商品的业务逻辑。整个业务逻辑由3个微服务提供支持：

- 仓储服务：对给定的商品扣除仓储数量。
- 订单服务：根据采购需求创建订单。
- 帐户服务：从用户帐户中扣除余额。

![](https://seata.io/zh-cn/assets/images/architecture-6bdb120b83710010167e8b75448505ec.png)

本来想直接使用官方示例工程 [incubator-seata-samples](https://github.com/apache/incubator-seata-samples) 中的 **springboot-dubbo-seata**，奈何应用启动报错，所以决定重新搭建应用，对于业务代码则copy示例工程的。根据 [新人文档](https://seata.io/zh-cn/docs/ops/deploy-guide-beginner) 中业务系统集成Client章节的内容可知，添加Seata依赖有三种方式：

- 依赖seata-all
- 依赖seata-spring-boot-starter，支持yml、properties配置(.conf可删除)，内部已依赖seata-all
- 依赖spring-cloud-starter-alibaba-seata，内部集成了seata，并实现了xid传递

**springboot-dubbo-seata** 示例工程中引入的是seata-all，一开始使用seata-all依赖，但是在TCC模式下有bug，所以我们选择 **spring-cloud-starter-alibaba-seata**，也更好用。

因为Spring Cloud Alibaba 2021.0.1.0版本包含的Seata版本是1.4.2，[Spring Cloud Alibaba版本说明](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)，所以需要排除spring-cloud-starter-alibaba-seata自带的starter，引入指定版本的seata-spring-boot-starter。starter会自动代理数据源和创建GlobalTransactionScanner，所以就不再需要 **SeataAutoConfig**。

```
<dependency>
	<groupId>io.seata</groupId>
	<artifactId>seata-spring-boot-starter</artifactId>
	<version>1.7.0</version>
</dependency>
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-seata</artifactId>
	<exclusions>
		<exclusion>
			<groupId>io.seata</groupId>
			<artifactId>seata-spring-boot-starter</artifactId>
		</exclusion>
	</exclusions>
</dependency>
```

## 三、运行 yimo-seata-samples

### 1. 下载代码

```
git clone https://github.com/htwdjqr/yimo-seata-samples.git
```

### 2. 数据库

创建 **seata** 数据库，导入sql文件夹下的db_seata.sql。
```
+-------------------------+
| Tables_in_seata         |
+-------------------------+
| t_account               |
| t_order                 |
| t_stock                 |
| undo_log                |
| tcc_fence_log           |
+-------------------------+
```

其中 **undo_log** 是AT模式下的回滚日志表，**tcc_fence_log** 是TCC模式下用于解决幂等、悬挂和空回滚问题，具体可以参考官网上的文章：[阿里 Seata 新版本终于解决了 TCC 模式的幂等、悬挂和空回滚问题。](https://seata.io/zh-cn/blog/seata-tcc-fence)

### 3. 启动 Nacos
Nacos使用的版本是1.4.2

- 官网地址：[https://nacos.io/zh-cn/docs/quick-start.html](https://nacos.io/zh-cn/docs/quick-start.html)
- 下载地址：[https://github.com/alibaba/nacos/releases/tag/1.4.2](https://github.com/alibaba/nacos/releases/tag/1.4.2)
- 下载后启动，Nacos控制台地址：http://127.0.0.1:8848/nacos/index.html

### 4. 启动 Seata Server

Seata Server使用1.7.0版本，在 [GitHub](https://github.com/apache/incubator-seata/tags) 中找到对应版本下载，解压后在bin目录下双击seata-server.bat即可启动。file模式下全局事务的数据存储在bin目录下的sessionStore文件夹中，如果应用调试过程中产生了脏数据导致事务提交/回滚有问题，可以删除文件夹中的文件重置。

### 5. 运行微服务

启动 AccountApplication、BusinessApplication、OrderApplication、StockApplication。

### 6. 验证分布式提交/回滚

AT模式接口代码在 **com.yimo.samples.business.controller.BusinessController#handleBusiness**，接口地址为：http://localhost:8104/business/dubbo/buy。

TCC模式接口代码在 **com.yimo.samples.business.controller.BusinessController#tccHandleBusiness**，接口地址为：http://localhost:8104/business/dubbo/tcc/buy。

body：

```json
{
    "userId":"1",
    "commodityCode":"C201901140001",
    "name":"fan",
    "count":2,
    "amount":"100"
}
```

分布式事务回滚可以注释 BusinessServiceImpl类中的代码模拟异常

```java
//打开注释测试事务发生异常后，全局回滚功能
if (!flag) {
    throw new RuntimeException("测试抛异常后，分布式事务回滚！");
}
```

除了在全局事务发起的business中测试异常回滚，还可以在微服务中测试异常回滚的情况，比如在 AccountServiceImpl。

```java
public ObjectResponse decreaseAccount(AccountDTO accountDTO) {
    int account = baseMapper.decreaseAccount(accountDTO.getUserId(), accountDTO.getAmount().doubleValue());
    int i = 1 / 0;
}
```

## 四、AT 模式事务隔离

AT模式的使用很简单，只需要在事务发起方添加 **@GlobalTransactional** 注解，其他的事情Seata自动完成，所以使用上没啥好说的，我们来关注下AT模式下的事务隔离，**yimo-seata-samples** 中也有对应的测试代码。

### 1. 读隔离

在数据库本地事务隔离级别 **读已提交（Read Committed）** 或以上的基础上，Seata（AT 模式）的默认全局隔离级别是 **读未提交（Read Uncommitted）** 。

如果应用在特定场景下，必需要求全局的 **读已提交** ，目前 Seata 的方式是通过 SELECT FOR UPDATE 语句的代理。

![Read Isolation: SELECT FOR UPDATE](https://img.alicdn.com/tfs/TB138wuwYj1gK0jSZFuXXcrHpXa-724-521.png)

SELECT FOR UPDATE 语句的执行会申请 **全局锁** ，如果 **全局锁** 被其他事务持有，则释放本地锁（回滚 SELECT FOR UPDATE 语句的本地执行）并重试。这个过程中，查询是被 block 住的，直到 **全局锁** 拿到，即读取的相关数据是 **已提交** 的，才返回。出于总体性能上的考虑，Seata 目前的方案并没有对所有 SELECT 语句都进行代理，仅针对 FOR UPDATE 的 SELECT 语句。

具体一点理解上面的读隔离，因为AT模式也是两阶段提交，在第一阶段的时候本地事务就已经提交了，所以此时被提交的数据就已经能被其他事务查询到，但如果后面在第二阶段事务被回滚，那在前面被其他事务读取到的数据就是脏数据了。

以AT模式下单接口为例，我们在 **com.yimo.samples.account.controller.AccountController#testReadIsolation** 增加测试读隔离的接口，

```java
    public ObjectResponse testReadIsolation() {
        AccountEntity account = baseMapper.getByUserId("1");
        System.out.println("account amount = " + account.getAmount());
        ObjectResponse response = new ObjectResponse();
        response.setStatus(RspStatusEnum.SUCCESS.getCode());
        response.setMessage(RspStatusEnum.SUCCESS.getMessage());
        response.setData(account);
        return response;
    }
```

在 BusinessServiceImpl的下单逻辑中增加睡眠30秒，让我们有足够的时间来观察数据的情况。

```java
public ObjectResponse handleBusiness(BusinessDTO businessDTO) {
        System.out.println("开始全局事务，XID = " + RootContext.getXID());
        ObjectResponse<Object> objectResponse = new ObjectResponse<>();
        //1、扣减库存
        //2、创建订单
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //打开注释测试事务发生异常后，全局回滚功能
        if (!flag) {
            throw new RuntimeException("测试抛异常后，分布式事务回滚！");
        }
}
```

在请求下单接口之后，接着我们请求账户服务的读隔离测试接口，[http://localhost:8102/account/test_read_isolation](http://localhost:8102/account/test_read_isolation)，会发现返回的账户余额是已经被下单扣减之后的，但是下单接口的全局事务还未提交，这就是脏读。或者在请求下单接口之后，我们通过Navicat来查询t_order、t_stock、t_account三个表的数据情况，也会发现已经插入订单数据，并且余额和库存都已经扣减。

Seata解决脏读的方案是：**select语句加for update，代理方法增加@GlobalLock+@Transactional或@GlobalTransactional**。注意：如果方法中只有查询，那就用@GlobalLock就行，若使用@GlobalTransactional注解就会增加一些没用的额外的rpc开销比如begin 返回xid，提交事务等。GlobalLock简化了rpc过程，使其做到更高的性能。代码改动如下：

```java
    @GlobalLock
    @Transactional(rollbackFor = Throwable.class)
    public ObjectResponse testReadIsolation() {
        AccountEntity account = baseMapper.getByUserId("1");
        System.out.println("Hi, i got lock, i will do some thing with holding this lock.");
        System.out.println("account amount = " + account.getAmount());
        ObjectResponse response = new ObjectResponse();
        response.setStatus(RspStatusEnum.SUCCESS.getCode());
        response.setMessage(RspStatusEnum.SUCCESS.getMessage());
        response.setData(account);
        return response;
    }
```

```java
    <select id="getByUserId" resultMap="BaseResultMap">
        select * from t_account where user_id = #{userId} for update
    </select>
```

改完之后再测试，请求下单接口之后，接着我们请求账户服务的读隔离测试接口会抛出异常，提示获取锁失败。

```j
### SQL: select * from t_account where user_id = ? for update
### Cause: io.seata.rm.datasource.exec.LockWaitTimeoutException: Global lock wait timeout
; Global lock wait timeout; nested exception is io.seata.rm.datasource.exec.LockWaitTimeoutException: Global lock wait timeout
```

```
Caused by: io.seata.rm.datasource.exec.LockConflictException: get lock failed, lockKey: t_account:1
	at io.seata.rm.datasource.ConnectionProxy.checkLock(ConnectionProxy.java:121) ~[seata-all-1.7.0.jar:1.7.0]
	at io.seata.rm.datasource.exec.SelectForUpdateExecutor.doExecute(SelectForUpdateExecutor.java:103) ~[seata-all-1.7.0.jar:1.7.0]
	... 94 common frames omitted
```

但这也仅限于在应用中才有用，如果是其他客户端，比如Navicat中查询也还是会查到脏数据。

### 2. 写隔离

- 一阶段本地事务提交前，需要确保先拿到 **全局锁** 。
- 拿不到 **全局锁** ，不能提交本地事务。
- 拿 **全局锁** 的尝试被限制在一定范围内，超出范围将放弃，并回滚本地事务，释放本地锁。

以一个示例来说明：

两个全局事务 tx1 和 tx2，分别对 a 表的 m 字段进行更新操作，m 的初始值 1000。

tx1 先开始，开启本地事务，拿到本地锁，更新操作 m = 1000 - 100 = 900。本地事务提交前，先拿到该记录的 **全局锁** ，本地提交释放本地锁。 tx2 后开始，开启本地事务，拿到本地锁，更新操作 m = 900 - 100 = 800。本地事务提交前，尝试拿该记录的 **全局锁** ，tx1 全局提交前，该记录的全局锁被 tx1 持有，tx2 需要重试等待 **全局锁** 。

![Write-Isolation: Commit](https://img.alicdn.com/tfs/TB1zaknwVY7gK0jSZKzXXaikpXa-702-521.png)

tx1 二阶段全局提交，释放 **全局锁** 。tx2 拿到 **全局锁** 提交本地事务。

![Write-Isolation: Rollback](https://img.alicdn.com/tfs/TB1xW0UwubviK0jSZFNXXaApXXa-718-521.png)

如果 tx1 的二阶段全局回滚，则 tx1 需要重新获取该数据的本地锁，进行反向补偿的更新操作，实现分支的回滚。

此时，如果 tx2 仍在等待该数据的 **全局锁**，同时持有本地锁，则 tx1 的分支回滚会失败。分支的回滚会一直重试，直到 tx2 的 **全局锁** 等锁超时，放弃 **全局锁** 并回滚本地事务释放本地锁，tx1 的分支回滚最终成功。

因为整个过程 **全局锁** 在 tx1 结束前一直是被 tx1 持有的，所以不会发生 **脏写** 的问题。

**Seata解决脏写的方式是在写数据的代理方法上也增加@GlobalTransactional注解，表示这个方法也要纳入分布式事务数据的管理范围。** 我们在 **com.yimo.samples.account.controller.AccountController#testWriteIsolation** 增加写隔离的测试接口。

```java
    //@GlobalTransactional
    public void testWriteIsolation() {
        baseMapper.decreaseAccount("1", Double.valueOf("100"));
        // System.out.println("Hi, i got lock, i will do some thing with holding this lock.");
    }
```

测试方法和读隔离的一样，在请求下单之后之后，接着请求写隔离的接口，[http://localhost:8102/account/test_write_isolation](http://localhost:8102/account/test_write_isolation)，写隔离接口正常的扣减了用户余额，但是下单接口就有问题了，看账户服务的日志

```
2024-01-19 18:26:06.043  INFO 32792 --- [ch_RMROLE_1_1_8] i.s.r.d.undo.AbstractUndoExecutor        : Field not equals, name amount, old value 2900.0, new value 2800.0
2024-01-19 18:26:06.046 ERROR 32792 --- [ch_RMROLE_1_1_8] i.seata.rm.datasource.DataSourceManager  : branchRollback failed. branchType:[AT], xid:[192.168.17.72:8091:3171014168140550177], branchId:[3171014168140550179], resourceId:[jdbc:mysql://127.0.0.1:3306/seata], applicationData:[null]. reason:[Branch session rollback failed because of dirty undo log, please delete the relevant undolog after manually calibrating the data. xid = 192.168.17.72:8091:3171014168140550177 branchId = 3171014168140550179]
2024-01-19 18:26:06.046  INFO 32792 --- [ch_RMROLE_1_1_8] io.seata.rm.AbstractRMHandler            : Branch Rollbacked result: PhaseTwo_RollbackFailed_Unretryable
```

全局事务回滚失败，因为回滚时会拿 undo_log 中的后镜与当前数据进行比较，如果有不同，说明数据被当前全局事务之外的动作做了修改，也就是被脏写了。

因为全局事务锁相关的数据是存储在Seata Server的，所以我们需要停掉Server，再将bin目录下sessionStore文件夹中的数据文件删除（file存储模式），在方法上增加 @GlobalTransactional 注解之后再进行测试。

```java
    @GlobalTransactional
    public void testWriteIsolation() {
        baseMapper.decreaseAccount("1", Double.valueOf("100"));
        System.out.println("Hi, i got lock, i will do some thing with holding this lock.");
    }
```

在请求下单之后之后，接着请求写隔离的接口，账户服务的写隔离接口会抛出异常，异常信息与上面读隔离的一致，获取全局失败。

所以AT模式下的事务隔离需要我们注意的是，想要避免脏读脏写，除了在分布式事务的主业务逻辑要加@GlobalTransactional的注解，在其他读写涉及到分布式事务操作的表的方法上也要加相应的注解才行。关于Seata事务隔离的两篇文章：

- [详解 Seata AT 模式事务隔离级别与全局锁设计](https://seata.io/zh-cn/blog/seata-at-lock)
- [Seata事务隔离](https://seata.io/zh-cn/docs/user/appendix/isolation)

## 五、TCC 模式

TCC接口定义实例如下：

```java
@LocalTCC
public interface AccountTccAction {
    /**
     * Prepare boolean.
     *
     * @param actionContext the action context
     * @param accountDTO    commodityDTO
     * @return the boolean
     */
    @TwoPhaseBusinessAction(name = "AccountTccActionOne", commitMethod = "commit", rollbackMethod = "rollback", useTCCFence = true)
    boolean prepare(BusinessActionContext actionContext, @BusinessActionContextParameter(paramName = "accountDTO") AccountDTO accountDTO);

    /**
     * Commit boolean.
     *
     * @param actionContext the action context
     * @return the boolean
     */
    boolean commit(BusinessActionContext actionContext);

    /**
     * Rollback boolean.
     *
     * @param actionContext the action context
     * @return the boolean
     */
    boolean rollback(BusinessActionContext actionContext);
}
```

- @LocalTCC：TCC参与者是本地bean则需要该注解，如果是RPC服务则不需要。
- @TwoPhaseBusinessAction：表示当前方法使用 TCC 模式管理事务提交，并标明了 Try，Confirm，Cancel 三个阶段。name属性，给当前事务注册了一个全局唯一的的 TCC bean name，useTCCFence=true来解决幂等、悬挂、空回滚的问题。
- @BusinessActionContextParameter 负责将参数传递到二阶段，可以通过 BusinessActionContext 获取。
- BusinessActionContext 表示TCC事务的上下文。

TCC接口实现：

```java
@Component
public class AccountTccActionImpl implements AccountTccAction {
    @Autowired
    private AccountDao accountDao;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean prepare(BusinessActionContext actionContext, AccountDTO accountDTO) {
        log.info("AccountTccAction prepare,xid={}", actionContext.getXid());
        int account = accountDao.decreaseAccount(accountDTO.getUserId(), accountDTO.getAmount().doubleValue());
        return account > 0;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean commit(BusinessActionContext actionContext) {
        log.info("AccountTccAction commit,xid={}", actionContext.getXid());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean rollback(BusinessActionContext actionContext) {
        log.info("AccountTccAction rollback,xid={}", actionContext.getXid());
        JSONObject jsonObject = (JSONObject) actionContext.getActionContext("accountDTO");
        AccountDTO accountDTO = jsonObject.toJavaObject(AccountDTO.class);
        accountDao.increaseAccount(accountDTO.getUserId(), accountDTO.getAmount().doubleValue());
        return true;
    }
}
```

需要特别说明的是：按照TCC理论，在try阶段做资源预留，在commit阶段才真正操作资源，rollback阶段回滚资源，上面的例子中，为了简单处理（实际业务不建议），在try阶段就直接扣除余额，所以commit阶段无需任何处理直接返回true，在rollback再将余额回滚加回去，不然就要修改账户表，增加一个冻结金额的字段。

在定义好 TCC 接口之后，我们可以像 AT 模式一样，通过 `@GlobalTransactional` 开启一个分布式事务。TCC模式下的幂等、悬挂和空回滚的问题在1.5.1版本得到解决，具体可以参考官网上的文章：[阿里 Seata 新版本终于解决了 TCC 模式的幂等、悬挂和空回滚问题](https://seata.io/zh-cn/blog/seata-tcc-fence)。

## 六、参考文档

- [Seata 是什么](https://seata.io/zh-cn/docs/overview/what-is-seata)
- [Seata 事务隔离](https://seata.io/zh-cn/docs/user/appendix/isolation)
- [新人文档](https://seata.io/zh-cn/docs/ops/deploy-guide-beginner)
- [详解 Seata AT 模式事务隔离级别与全局锁设计](https://seata.io/zh-cn/blog/seata-at-lock)
- [阿里 Seata 新版本终于解决了 TCC 模式的幂等、悬挂和空回滚问题](https://seata.io/zh-cn/blog/seata-tcc-fence)

