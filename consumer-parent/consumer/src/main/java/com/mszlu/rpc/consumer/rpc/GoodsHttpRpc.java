package com.mszlu.rpc.consumer.rpc;


import com.mszlu.rpc.annontation.MsHttpClient;
import com.mszlu.rpc.annontation.MsMapping;
import com.mszlu.rpc.provider.service.modal.Goods;
import org.springframework.web.bind.annotation.PathVariable;

@MsHttpClient(value = "goodsHttpRpc")
public interface GoodsHttpRpc {

    @MsMapping(url = "http://localhost:7777",api = "/provider/goods/{id}")
    public Goods findGoods(@PathVariable Long id);
}