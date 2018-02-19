package com.gaojiancheng.netty_learn.time;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author:Wilder Gao
 * @time:2017/12/12
 * @Discription：
 */
public class TimeServerHandler extends SimpleChannelInboundHandler<Object>{


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        System.out.println("Server start read");
        //类似于ByteBuffer
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);
        String body = new String(req,"UTF-8");

        System.out.println("The time server receive order:"+body);
        String currentTime = "Query Time Order".equalsIgnoreCase(body) ? new java.util.Date(
                System.currentTimeMillis()).toString() : "Bad Order";

        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        channelHandlerContext.write(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
