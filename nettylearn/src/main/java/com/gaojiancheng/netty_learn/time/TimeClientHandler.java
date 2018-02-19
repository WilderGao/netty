package com.gaojiancheng.netty_learn.time;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

/**
 * @author:Wilder Gao
 * @time:2017/12/12
 * @Discription：
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter{
    private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());
    private final ByteBuf firstMsG;

    public TimeClientHandler(){
        byte[] req = "QUERY TIME ORDER".getBytes();
        firstMsG = Unpooled.buffer(req.length);
        firstMsG.writeBytes(req);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(firstMsG);
    }

    @Override
    /**
     * 服务器发送过来的消息可以在这里接收到
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);
        String body = new String(req,"utf-8");
        System.out.println("Now is :"+body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
