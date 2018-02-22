package com.gaojiancheng.netty_learn.protobuf;

import com.gaojiancheng.netty_learn.proto.SubscribeReq;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:Wilder Gao
 * @time:2018/2/22
 * @Discription：
 */
public class SubReqClientHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        System.out.println("服务器返回的消息:  ");
        System.out.println(o);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int num = 10;
        for (int i = 0 ; i < num ; i++){
            ctx.write(subReq(i));
        }
        ctx.flush();
    }

    private SubscribeReq.Subscribe subReq(int i){
        SubscribeReq.Subscribe.Builder builder = SubscribeReq.Subscribe.newBuilder();
        builder.setUserName("WilderGao");
        builder.setSubReqID(i);
        builder.setProductName("Netty book for ProtoBuf");
        List<String> address = new ArrayList<>();
        address.add("GuangZhou GaoJianCheng");
        address.add("GuangZhou HuangGuiChun");
        builder.addAllAddress(address);
        return builder.build();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("===== found exception =====");
        cause.printStackTrace();
        ctx.close();
    }
}
