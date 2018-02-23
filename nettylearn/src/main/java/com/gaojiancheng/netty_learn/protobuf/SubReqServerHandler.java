package com.gaojiancheng.netty_learn.protobuf;

import com.gaojiancheng.netty_learn.proto.SubscribeReq;
import com.gaojiancheng.netty_learn.proto.SubscribeResp;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author:Wilder Gao
 * @time:2018/2/22
 * @Discription：
 */
public class SubReqServerHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o)
            throws Exception {
        SubscribeReq.Subscribe subscribe = (SubscribeReq.Subscribe) o;
        if (subscribe.getUserName().equalsIgnoreCase("WilderGao")){
            System.out.println("Server accept client ..."+subscribe.toString());
            channelHandlerContext.writeAndFlush(resp(subscribe.getSubReqID()));
        }
    }

    /**
     * 逻辑详细的描述了构建一个序列化ProtoBuf对象的过程
     * 首先要Builder，然后添加相应的参数
     * addAllAddress 则表明添加的是集合
     * @param subReqID
     * @return
     */
    private SubscribeResp.Subscriberesp resp(int subReqID){
        SubscribeResp.Subscriberesp.Builder builder = SubscribeResp.Subscriberesp.newBuilder();
        builder.setSubReqID(subReqID);
        builder.setRespCode(0);
        builder.setDesc("Netty book order succeed ,3 days later , sent to the designated address");
        return builder.build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("=====have exception=====");
        cause.printStackTrace();
        ctx.close();
    }
}
