package com.yimo.samples.account.service;

import com.yimo.samples.account.entity.AccountEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yimo.samples.common.dto.AccountDTO;
import com.yimo.samples.common.response.ObjectResponse;

/**
 *  服务类
 *
 * @author coder
 * @date 2024-01-12
 */
public interface IAccountService extends IService<AccountEntity> {

    /**
     * 扣用户钱
     */
    ObjectResponse decreaseAccount(AccountDTO accountDTO);


    void testGlobalLock();

    void testWriteIsolation();

}
