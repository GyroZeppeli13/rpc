package com.mszlu.rpc.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {
	//读取协议的序列化类型，来此枚举进行匹配
    PROTOSTUFF((byte) 0x01, "protostuff"),
    KRYO((byte) 0x02, "Kryo"),
    HESSIAN((byte) 0x03, "Hessian");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

}