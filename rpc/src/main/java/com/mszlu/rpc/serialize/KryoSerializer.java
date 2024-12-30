package com.mszlu.rpc.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.mszlu.rpc.constants.SerializationTypeEnum;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mszlu.rpc.exception.MsRpcException;
import com.mszlu.rpc.message.MsRequest;
import com.mszlu.rpc.message.MsResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


/**
 * @description Kryo序列化器
 */
public class KryoSerializer implements Serializer{

    @Override
    public String name() {
        return SerializationTypeEnum.KRYO.getName();
    }

    /**
     * Because Kryo is not thread safe. So, use ThreadLocal to store Kryo objects
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // Kryo 需要在序列化/反序列化过程中知道要处理的类，因此需要注册这些类
        kryo.register(MsResponse.class);
        kryo.register(MsRequest.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new MsRpcException("Serialization failed");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // byte->Object:从byte数组中反序列化出对象
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(o);
        } catch (Exception e) {
            throw new MsRpcException("Deserialization failed");
        }
    }
}