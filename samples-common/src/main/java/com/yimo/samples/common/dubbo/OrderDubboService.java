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
package com.yimo.samples.common.dubbo;


import com.yimo.samples.common.response.ObjectResponse;
import com.yimo.samples.common.dto.OrderDTO;

/**
 * @Author: heshouyou
 * @Description 订单服务接口
 * @Date Created in 2019/1/13 16:28
 */
public interface OrderDubboService {

    /**
     * 创建订单
     */
    ObjectResponse<OrderDTO> createOrder(OrderDTO orderDTO);

    /**
     * 创建订单——TCC模式
     *
     * @param orderDTO
     * @return
     */
    ObjectResponse<OrderDTO> tccCreateOrder(OrderDTO orderDTO);
}
