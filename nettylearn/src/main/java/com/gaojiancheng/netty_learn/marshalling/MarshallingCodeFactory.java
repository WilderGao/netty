package com.gaojiancheng.netty_learn.marshalling;

import io.netty.handler.codec.marshalling.*;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

/**
 * @author:Wilder Gao
 * @time:2018/3/2
 * @Discription：
 */
public final class MarshallingCodeFactory {

    /**
     * 创建Marshalling 解码器
     * @return
     */
    public static MarshallingDecoder buildMarshallingDecoder(){
        //首先创建MarshallerFactory 实例
        final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
        //接下来创建MarshallingConfiguration
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        //根据factory 和 configuration 创建 MarshallerProvider
        UnmarshallerProvider provider = new DefaultUnmarshallerProvider(
                marshallerFactory , configuration);
        MarshallingDecoder decoder = new MarshallingDecoder(provider , 1024);
        return decoder ;

    }

    public static MarshallingEncoder buildMarshallingEncoder(){
        final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        MarshallerProvider provider = new DefaultMarshallerProvider(
                marshallerFactory , configuration);
        MarshallingEncoder encoder = new MarshallingEncoder(provider);
        return encoder;
    }
}
