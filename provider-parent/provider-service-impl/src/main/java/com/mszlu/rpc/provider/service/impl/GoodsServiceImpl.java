package com.mszlu.rpc.provider.service.impl;

import com.mszlu.rpc.provider.service.GoodsService;
import com.mszlu.rpc.provider.service.modal.Goods;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class GoodsServiceImpl implements GoodsService {

    public Goods findGoods(Long id) {
        return new Goods(id,"服务提供方商品", BigDecimal.valueOf(100));
    }
}
