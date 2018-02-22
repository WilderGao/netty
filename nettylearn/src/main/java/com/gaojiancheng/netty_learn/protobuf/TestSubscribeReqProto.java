package com.gaojiancheng.netty_learn.protobuf;


import com.gaojiancheng.netty_learn.proto.SubscribeReq;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:Wilder Gao
 * @time:2018/2/22
 * @Discription：对Google的ProtoBuf练习测试
 */
public class TestSubscribeReqProto {
    private static byte[] encode(SubscribeReq.Subscribe req){
        return req.toByteArray();
    }

    private static SubscribeReq.Subscribe decode(byte[] body) throws InvalidProtocolBufferException {
        return SubscribeReq.Subscribe.parseFrom(body);
    }

    private static SubscribeReq.Subscribe createSubscribeReq(){
        SubscribeReq.Subscribe.Builder builder = SubscribeReq.Subscribe.newBuilder();
        builder.setSubReqID(1);
        builder.setUserName("WilderGao");
        builder.setProductName("Netty Book");
        List<String> address = new ArrayList<>();
        address.add("GuangZhou PurpleHuang");
        address.add("GuangZhou JianChengGao");
        builder.addAllAddress(address);

        return builder.build();
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
        SubscribeReq.Subscribe subscribe = createSubscribeReq();
        System.out.println("Before encode"+subscribe.toString());
        SubscribeReq.Subscribe req = decode(encode(subscribe));
        System.out.println("After decode"+req.toString());
        System.out.println("Assert equal :"+subscribe.equals(req));
    }

}
