package com.mszlu.rpc.serialize;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.mszlu.rpc.constants.SerializationTypeEnum;
import com.mszlu.rpc.exception.MsRpcException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @description Hessian序列化器
 */
public class HessianSerializer implements Serializer {
    @Override
    public String name() {
        return SerializationTypeEnum.HESSIAN.getName();
    }

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new MsRpcException("Serialization failed");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            Object o = hessianInput.readObject();
            return clazz.cast(o);
        } catch (Exception e) {
            throw new MsRpcException("Deserialization failed");
        }
    }
}