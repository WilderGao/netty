package com.gaojiancheng.netty_learn.helloworld;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author:Wilder Gao
 * @time:2018/2/18
 * @Discription：使用netty第一步，hello World篇
 * 当有很多个客户端或者一个客户端在很短的时间内发送很多的信息到服务器端时，可能会导致TCP粘包的情况发生
 * 解决的方法是使用Netty中的三种解码器的应用
 * LineBasedFrameDecoder 解码器，按照 \n、\r\n 进行解码
 * DelimiterBasedFrameDecoder 分隔符解码器，自己设定要用什么符号来划分
 * FixedLengthFrameDecoder 定长解码器，按照固定的长度进行解码
 */
public class HelloServer {
    Logger logger = LoggerFactory.getLogger(HelloServer.class);
    private int port;
    public HelloServer(int port){
        this.port = port;
    }

    public void bind(){
        //处理NIO事件的多线程循环器，一个用来接收新来的连接，一个用来处理已经被接收的连接
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //启动NIO配置的服务类
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(eventLoopGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //childHandler 在客户端执行的时候才会触发
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new HelloServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024);
            ChannelFuture future = bootstrap.bind(port).sync();
            //服务器监听端口，阻塞模式，直到服务器链路关闭之后main才关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            eventLoopGroup.shutdownGracefully();
        }
    }

    private class HelloServerHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //============以下是接收到消息的逻辑处理===============
            logger.info("接收到客户端发来的消息");
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] result = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(result);
            String receiveString = new String(result,"utf-8");
            logger.info("内容为"+receiveString);
            //释放资源
            byteBuf.release();

            //===========以下是返回消息的逻辑处理===============
            String response = "Hello world";
            //返回消息时一定要将它打包成ByteBuf 格式
            ByteBuf encode = ctx.alloc().buffer(4 * response.length());
            encode.writeBytes(response.getBytes());
            ctx.writeAndFlush(encode);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.error("===========出现异常========");
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }
    }

    public static void main(String[] args) {
        new HelloServer(8080).bind();
    }
}
