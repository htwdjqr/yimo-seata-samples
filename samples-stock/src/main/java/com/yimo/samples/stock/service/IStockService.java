package com.yimo.samples.stock.service;

import com.yimo.samples.common.dto.CommodityDTO;
import com.yimo.samples.common.response.ObjectResponse;
import com.yimo.samples.stock.entity.StockEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *  服务类
 *
 * @author coder
 * @date 2024-01-12
 */
public interface IStockService extends IService<StockEntity> {

    /**
     * 扣减库存
     */
    ObjectResponse decreaseStock(CommodityDTO commodityDTO);
}
