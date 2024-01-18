package com.yimo.samples.account.tcc;

import com.yimo.samples.common.dto.AccountDTO;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * 这是描述
 *
 * @author 会跳舞的机器人
 * @date 2024/1/16
 */
@LocalTCC
public interface AccountTccAction {
    /**
     * Prepare boolean.
     *
     * @param actionContext the action context
     * @param accountDTO    commodityDTO
     * @return the boolean
     */
    @TwoPhaseBusinessAction(name = "AccountTccActionOne", commitMethod = "commit", rollbackMethod = "rollback")
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
