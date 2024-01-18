package com.yimo.samples.stock.service.impl;

import com.yimo.samples.common.dto.CommodityDTO;
import com.yimo.samples.common.enums.RspStatusEnum;
import com.yimo.samples.common.response.ObjectResponse;
import com.yimo.samples.stock.entity.StockEntity;
import com.yimo.samples.stock.dao.StockDao;
import com.yimo.samples.stock.service.IStockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 *  服务实现类
 *
 * @author coder
 * @date 2024-01-12
 */
@Service
public class StockServiceImpl extends ServiceImpl<StockDao, StockEntity> implements IStockService {

    @Override
    public ObjectResponse decreaseStock(CommodityDTO commodityDTO) {
        int stock = baseMapper.decreaseStock(commodityDTO.getCommodityCode(), commodityDTO.getCount());
        ObjectResponse<Object> response = new ObjectResponse<>();
        if (stock > 0) {
            response.setStatus(RspStatusEnum.SUCCESS.getCode());
            response.setMessage(RspStatusEnum.SUCCESS.getMessage());
            return response;
        }

        response.setStatus(RspStatusEnum.FAIL.getCode());
        response.setMessage(RspStatusEnum.FAIL.getMessage());
        return response;
    }
}
