package com.mszlu.rpc.provider.service.impl;

import com.mszlu.rpc.annontation.MsService;
import com.mszlu.rpc.provider.service.GoodsService;
import com.mszlu.rpc.provider.service.modal.Goods;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
//在service的实现类加上注解，代表将GoodsService下的所有方法发布为服务
@MsService
public class GoodsServiceImpl implements GoodsService {

    public Goods findGoods(Long id) {
        return new Goods(id,"服务提供方商品", BigDecimal.valueOf(100));
    }
}
