package com.yimo.samples.account.dao;

import com.yimo.samples.account.entity.AccountEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 *  Mapper 接口
 *
 * @author coder
 * @date 2024-01-12
 */
public interface AccountDao extends BaseMapper<AccountEntity> {

    int decreaseAccount(@Param("userId") String userId, @Param("amount") Double amount);

    AccountEntity testGlobalLock(@Param("userId") String userId);

    int testWriteIsolation(@Param("userId") String userId, @Param("amount") Double amount);

}
