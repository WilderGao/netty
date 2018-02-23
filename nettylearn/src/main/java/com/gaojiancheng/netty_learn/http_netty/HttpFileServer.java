package com.gaojiancheng.netty_learn.http_netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author:Wilder Gao
 * @time:2018/2/23
 * @Discription：
 */
public class HttpFileServer {
    private String DEFAULT_URL = "/src/main/java/com/gaojiancheng/netty_learn/";

    public void run(final int port , final String url){
        EventLoopGroup workGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(workGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("http-encoder", new HttpRequestEncoder())
                                    .addLast("http-decoder", new HttpRequestDecoder())
                                    .addLast("file-handler", new HttpFileServerHandler(url))
                                    .addLast("chunk-handler", new ChunkedWriteHandler())
                                    .addLast("http-aggregator", new HttpObjectAggregator(65536));
                        }
                    });
            ChannelFuture future = bootstrap.bind("localhost" , port).sync();
            System.out.println("HTTP 文件服务器启动，地址为："+"http://localhost:"+port);
            future.channel().close().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }
}
