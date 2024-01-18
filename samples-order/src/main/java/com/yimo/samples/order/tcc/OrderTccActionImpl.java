package com.yimo.samples.order.tcc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yimo.samples.common.dto.AccountDTO;
import com.yimo.samples.common.dto.OrderDTO;
import com.yimo.samples.common.dubbo.AccountDubboService;
import com.yimo.samples.common.response.ObjectResponse;
import com.yimo.samples.order.dao.OrderDao;
import com.yimo.samples.order.entity.OrderEntity;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
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
public class OrderTccActionImpl implements OrderTccAction {
    @DubboReference(version = "1.0.0", check = false)
    private AccountDubboService accountDubboService;
    @Autowired
    private OrderDao orderDao;


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean prepare(BusinessActionContext actionContext, OrderDTO orderDTO) {
        log.info("OrderTccAction prepare,xid={}", actionContext.getXid());
        //扣减用户账户
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId(orderDTO.getUserId());
        accountDTO.setAmount(orderDTO.getOrderAmount());
        ObjectResponse objectResponse = accountDubboService.tccDecreaseAccount(accountDTO);

        if (objectResponse.getStatus() != 200) {
            log.info("tccDecreaseAccount fail,response={}", JSON.toJSONString(objectResponse));
            return false;
        }
        //生成订单
        OrderEntity tOrder = new OrderEntity();
        BeanUtils.copyProperties(orderDTO, tOrder);
        tOrder.setCount(orderDTO.getOrderCount());
        tOrder.setAmount(orderDTO.getOrderAmount().doubleValue());
        try {
            orderDao.createOrder(tOrder);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean commit(BusinessActionContext actionContext) {
        log.info("OrderTccAction commit,xid={}", actionContext.getXid());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean rollback(BusinessActionContext actionContext) {
        log.info("OrderTccAction rollback,xid={}", actionContext.getXid());
        JSONObject jsonObject = (JSONObject) actionContext.getActionContext("orderDTO");
        OrderDTO orderDTO = jsonObject.toJavaObject(OrderDTO.class);
        orderDao.deleteByOrderNo(orderDTO.getOrderNo());
        return true;
    }
}
