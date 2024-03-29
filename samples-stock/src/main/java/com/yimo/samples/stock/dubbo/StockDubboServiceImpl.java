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
package com.yimo.samples.stock.dubbo;

import com.yimo.samples.common.dto.CommodityDTO;
import com.yimo.samples.common.dubbo.StockDubboService;
import com.yimo.samples.common.enums.RspStatusEnum;
import com.yimo.samples.common.response.ObjectResponse;
import com.yimo.samples.stock.service.IStockService;
import com.yimo.samples.stock.tcc.StockTccAction;
import io.seata.core.context.RootContext;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: heshouyou
 * @Description
 * @Date Created in 2019/1/23 16:13
 */
@DubboService(version = "1.0.0", protocol = "${dubbo.protocol.id}", application = "${dubbo.application.id}",
        registry = "${dubbo.registry.id}", timeout = 3000)
public class StockDubboServiceImpl implements StockDubboService {

    @Autowired
    private IStockService stockService;
    @Autowired
    private StockTccAction stockTccAction;

    @Override
    public ObjectResponse decreaseStock(CommodityDTO commodityDTO) {
        System.out.println("全局事务id ：" + RootContext.getXID());
        return stockService.decreaseStock(commodityDTO);
    }

    @Override
    public ObjectResponse tccDecreaseStock(CommodityDTO commodityDTO) {
        System.out.println("全局事务id ：" + RootContext.getXID());
        boolean flag = stockTccAction.prepare(null, commodityDTO);
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
