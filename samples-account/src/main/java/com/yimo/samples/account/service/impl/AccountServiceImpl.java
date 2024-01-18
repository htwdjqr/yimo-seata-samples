package com.yimo.samples.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimo.samples.account.dao.AccountDao;
import com.yimo.samples.account.entity.AccountEntity;
import com.yimo.samples.account.service.IAccountService;
import com.yimo.samples.common.dto.AccountDTO;
import com.yimo.samples.common.enums.RspStatusEnum;
import com.yimo.samples.common.response.ObjectResponse;
import io.seata.spring.annotation.GlobalLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现类
 *
 * @author coder
 * @date 2024-01-12
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountDao, AccountEntity> implements IAccountService {

    @Override
    public ObjectResponse decreaseAccount(AccountDTO accountDTO) {
        int account = baseMapper.decreaseAccount(accountDTO.getUserId(), accountDTO.getAmount().doubleValue());
        ObjectResponse<Object> response = new ObjectResponse<>();
        if (account > 0) {
            response.setStatus(RspStatusEnum.SUCCESS.getCode());
            response.setMessage(RspStatusEnum.SUCCESS.getMessage());
            return response;
        }

        response.setStatus(RspStatusEnum.FAIL.getCode());
        response.setMessage(RspStatusEnum.FAIL.getMessage());
        return response;
    }

    @Override
    @GlobalLock
    @Transactional(rollbackFor = {Throwable.class})
    public void testGlobalLock() {
        baseMapper.testGlobalLock("1");
        System.out.println("Hi, i got lock, i will do some thing with holding this lock.");
    }

    @Override
    //@GlobalTransactional
    public void testWriteIsolation() {
        baseMapper.testWriteIsolation("1", Double.valueOf("100"));
        System.out.println("Hi, i got lock, i will do some thing with holding this lock.");
    }
}
