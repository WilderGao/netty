package com.gaojiancheng.netty_learn.http_netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author:Wilder Gao
 * @time:2018/2/23
 * @Discription：
 */
public class HttpFileServer {
    private static String DEFAULT_URL = "/nettylearn/src/main/java/com/gaojiancheng/netty_learn/";

    public void run(final int port , final String url){
        EventLoopGroup workGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(workGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast("http-decoder", new HttpRequestDecoder())
                                    .addLast("http-aggregator", new HttpObjectAggregator(65536))
                                    .addLast("http-encoder", new HttpResponseEncoder())
                                    .addLast("chunk-handler", new ChunkedWriteHandler())
                                    .addLast("file-handler", new HttpFileServerHandler(url));
                        }
                    });
            ChannelFuture future = bootstrap.bind("localhost" , port).sync();
            System.out.println("HTTP 文件服务器启动，地址为：http://localhost:"+port+url);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args)throws Exception {
        int port = 8888;
        if(args.length > 0)
        {
            try{
                port = Integer.parseInt(args[0]);
            }catch(NumberFormatException e){
                port = 8080;
            }
        }
        String url = DEFAULT_URL;
        if(args.length > 1) {
            url = args[1];
        }
        new HttpFileServer().run(port, url);
    }
}
