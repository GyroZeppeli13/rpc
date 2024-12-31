package com.mszlu.rpc.provider.service.impl;

import com.mszlu.rpc.annontation.MsService;
import com.mszlu.rpc.provider.service.GoodsService;
import com.mszlu.rpc.provider.service.modal.Goods;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
//在service的实现类加上注解，代表将GoodsService下的所有方法发布为服务
@MsService
public class GoodsServiceImpl implements GoodsService {

//    public Goods findGoods(Long id) {
//        return new Goods(id,"我是", BigDecimal.valueOf(100));
//    }

    public Goods findGoods(Long id) {
        String goodsName = "我是";
        System.out.println("GoodsName in UTF-8 bytes: " + Arrays.toString(goodsName.getBytes(StandardCharsets.UTF_8)));
        return new Goods(id, goodsName, BigDecimal.valueOf(100));
    }
}
