package com.gaojiancheng.netty_learn.time;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author:Wilder Gao
 * @time:2017/12/12
 * @Discription：
 */
public class TimeServer {

    public void bind(int port){
        //配置服务端的NIO线程组，包含了一组NIO线程，专门用于网络事件的处理，Reactor线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //创建两个线程组的原因是一个用于服务端接收客户端的信息，一个用于进行SocketChannel的网络读写
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //ServerBookStrap对象，Netty用于启动NIO服务端的辅助启动类，目的是降低服务端的开发复杂度
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup)
                //设置创建的channel为NioServerSocketChannel
                .channel(NioServerSocketChannel.class)
                //配置NioServerSocketChannel的TCP参数
                .option(ChannelOption.SO_BACKLOG,1024)
                //绑定I/O事件的处理类
                .childHandler(new ChildChannelHandler());

            //绑定端口，同步等待成功，返回的future用于异步操作的通知回调
            ChannelFuture future = bootstrap.bind(port).sync();
            //等待服务器监听端口关闭，使用这个方法进行阻塞，等待服务端链路关闭之后main函数才退出
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        new TimeServer().bind(port);
    }
}
