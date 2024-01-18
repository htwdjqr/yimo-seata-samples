package com.yimo.samples.order.tcc;

import com.yimo.samples.common.dto.OrderDTO;
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
public interface OrderTccAction {
    /**
     * Prepare boolean.
     *
     * @param actionContext the action context
     * @param orderDTO      OrderDTO
     * @return the boolean
     */
    @TwoPhaseBusinessAction(name = "OrderTccActionOne", commitMethod = "commit", rollbackMethod = "rollback")
    boolean prepare(BusinessActionContext actionContext, @BusinessActionContextParameter(paramName = "orderDTO") OrderDTO orderDTO);

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
