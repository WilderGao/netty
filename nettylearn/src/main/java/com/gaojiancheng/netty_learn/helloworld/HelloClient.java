package com.gaojiancheng.netty_learn.helloworld;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author:Wilder Gao
 * @time:2018/2/18
 * @Discription：hello world服务器端
 */
public class HelloClient {
    Logger logger = LoggerFactory.getLogger(HelloClient.class);

    /**
     * 连接服务器端
     * @param ip
     * @param port
     */
    public void connect(String ip , int port){
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        //客户端使用BootStrap
        try {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE , true)

                //handler 方法在初始化时就会执行
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024))
                                .addLast(new StringDecoder())
                                .addLast(new ClientHandler());
                    }
                });
            ChannelFuture future = bootstrap.connect(ip, port).sync();

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            clientGroup.shutdownGracefully();
        }
    }

    private class ClientHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] result = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(result);
            String receiveString = new String(result,"utf-8");
            logger.info("=======服务器端返回内容来了======\n"+receiveString);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            //连接成功之后向服务器发送消息
            String message = format.format(date);
            ByteBuf encode = ctx.alloc().buffer(4 * message.length());
            encode.writeBytes(message.getBytes());
            ctx.writeAndFlush(encode);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            //关闭Channel
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void main(String[] args) {
        new HelloClient().connect("127.0.0.1",8080);
    }
}
