package com.yimo.samples.stock.tcc;

import com.alibaba.fastjson.JSONObject;
import com.yimo.samples.common.dto.CommodityDTO;
import com.yimo.samples.stock.dao.StockDao;
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
public class StockTccActionImpl implements StockTccAction {
    @Autowired
    private StockDao stockDao;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean prepare(BusinessActionContext actionContext, CommodityDTO commodityDTO) {
        log.info("StockTccAction prepare,xid={}", actionContext.getXid());
        int stock = stockDao.decreaseStock(commodityDTO.getCommodityCode(), commodityDTO.getCount());
        return stock > 0;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean commit(BusinessActionContext actionContext) {
        log.info("StockTccAction commit,xid={}", actionContext.getXid());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean rollback(BusinessActionContext actionContext) {
        log.info("StockTccAction rollback,xid={}", actionContext.getXid());
        JSONObject jsonObject = (JSONObject) actionContext.getActionContext("commodityDTO");
        CommodityDTO commodityDTO = jsonObject.toJavaObject(CommodityDTO.class);
        stockDao.increaseStock(commodityDTO.getCommodityCode(), commodityDTO.getCount());
        return true;
    }
}
