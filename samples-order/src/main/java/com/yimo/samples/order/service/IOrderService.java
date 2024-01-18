package com.yimo.samples.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yimo.samples.common.dto.OrderDTO;
import com.yimo.samples.common.response.ObjectResponse;
import com.yimo.samples.order.entity.OrderEntity;

/**
 *  服务类
 *
 * @author coder
 * @date 2024-01-12
 */
public interface IOrderService extends IService<OrderEntity> {
    /**
     * 创建订单
     */
    ObjectResponse<OrderDTO> createOrder(OrderDTO orderDTO);

}
