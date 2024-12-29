package com.mszlu.rpc.exception;

public class MsRpcException extends RuntimeException {

    public MsRpcException(){
        super();
    }

    public MsRpcException(String msg){
        super(msg);
    }

    public MsRpcException(String msg,Exception e){
        super(msg,e);
    }
}