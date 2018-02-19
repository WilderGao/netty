package com.gaojiancheng.netty_learn.time;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author:Wilder Gao
 * @time:2017/12/12
 * @Discription：
 */
public class TimeClient {

    public void connect(int port , String host){
        //配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            //客户端是NioSocketChannel
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY,true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                    //作用是当创建NioSocketChannel成功之后，在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理I/O事件
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new TimeClientHandler());
                    }
                });

        //发起异步连接操作，进行连接
            ChannelFuture future = bootstrap.connect(host,port).sync();
            //等待客户端链路关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        new TimeClient().connect(port,"127.0.0.1");
    }
}
