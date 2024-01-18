/*
 *  Copyright 1999-2021 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yimo.samples.account.dubbo;

import com.yimo.samples.account.service.impl.AccountServiceImpl;
import com.yimo.samples.account.tcc.AccountTccAction;
import com.yimo.samples.common.dto.AccountDTO;
import com.yimo.samples.common.dubbo.AccountDubboService;
import com.yimo.samples.common.enums.RspStatusEnum;
import com.yimo.samples.common.response.ObjectResponse;
import io.seata.core.context.RootContext;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: heshouyou
 * @Description Dubbo Api Impl
 * @Date Created in 2019/1/23 14:40
 */
@DubboService(version = "1.0.0", protocol = "${dubbo.protocol.id}", application = "${dubbo.application.id}",
        registry = "${dubbo.registry.id}", timeout = 3000)
public class AccountDubboServiceImpl implements AccountDubboService {

    @Autowired
    private AccountServiceImpl accountService;
    @Autowired
    private AccountTccAction accountTccAction;

    @Override
    public ObjectResponse decreaseAccount(AccountDTO accountDTO) {
        System.out.println("全局事务id ：" + RootContext.getXID());
        return accountService.decreaseAccount(accountDTO);
    }

    @Override
    public ObjectResponse tccDecreaseAccount(AccountDTO accountDTO) {
        System.out.println("全局事务id ：" + RootContext.getXID());
        boolean flag = accountTccAction.prepare(null, accountDTO);

        ObjectResponse<Object> response = new ObjectResponse<>();
        if (flag) {
            response.setStatus(RspStatusEnum.SUCCESS.getCode());
            response.setMessage(RspStatusEnum.SUCCESS.getMessage());
            return response;
        }
        response.setStatus(RspStatusEnum.FAIL.getCode());
        response.setMessage(RspStatusEnum.FAIL.getMessage());
        return response;
    }
}
