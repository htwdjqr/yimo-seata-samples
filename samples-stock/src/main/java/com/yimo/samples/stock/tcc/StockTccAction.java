package com.yimo.samples.stock.tcc;

import com.yimo.samples.common.dto.CommodityDTO;
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
public interface StockTccAction {
    /**
     * Prepare boolean.
     *
     * @param actionContext the action context
     * @param commodityDTO  commodityDTO
     * @return the boolean
     */
    @TwoPhaseBusinessAction(name = "StockTccActionOne", commitMethod = "commit", rollbackMethod = "rollback", useTCCFence = true)
    boolean prepare(BusinessActionContext actionContext, @BusinessActionContextParameter(paramName = "commodityDTO") CommodityDTO commodityDTO);

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
