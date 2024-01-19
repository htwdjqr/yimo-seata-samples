package com.yimo.samples.account.tcc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yimo.samples.account.dao.AccountDao;
import com.yimo.samples.account.entity.AccountEntity;
import com.yimo.samples.common.dto.AccountDTO;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 这是描述
 *
 * @author 会跳舞的机器人
 * @date 2024/1/16
 */
@Slf4j
@Component
public class AccountTccActionImpl implements AccountTccAction {
    @Autowired
    private AccountDao accountDao;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean prepare(BusinessActionContext actionContext, AccountDTO accountDTO) {
        log.info("AccountTccAction prepare,xid={}", actionContext.getXid());
        int account = accountDao.decreaseAccount(accountDTO.getUserId(), accountDTO.getAmount().doubleValue());
        AccountEntity entity = accountDao.getByUserId(accountDTO.getUserId());
        log.info("prepare,account = {}", JSON.toJSONString(entity));
        // 这里抛出异常，本地事务回滚一次，全局事务又执行一次rollback的逻辑，相当于回滚了两次，导致数据错误
        //int i = 1 / 0;
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
        AccountEntity entity = accountDao.getByUserId(accountDTO.getUserId());
        log.info("rollback,account = {}", JSON.toJSONString(entity));
        accountDao.increaseAccount(accountDTO.getUserId(), accountDTO.getAmount().doubleValue());
        return true;
    }
}
