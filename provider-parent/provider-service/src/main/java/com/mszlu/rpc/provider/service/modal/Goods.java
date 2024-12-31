package com.mszlu.rpc.provider.service.modal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goods implements Serializable {

    //商品id
    private Long id;
    //商品名称
    private String goodsName;
    //商品价格
    private BigDecimal price;
}
