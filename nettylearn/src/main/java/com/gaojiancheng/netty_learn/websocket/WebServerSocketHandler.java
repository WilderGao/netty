package com.gaojiancheng.netty_learn.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.Date;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * @author:Wilder Gao
 * @time:2018/2/19
 * @Discription：WebSocket 连接处理类
 */
public class WebServerSocketHandler extends SimpleChannelInboundHandler<Object> {
    private Logger logger = LoggerFactory.getLogger(WebServerSocketHandler.class.getName());
    private WebSocketServerHandshaker handshaker ;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        //传统的HTTP接入
        if (o instanceof FullHttpRequest){
            logger.info("建立webSocket 握手连接");
            handleHttpRequest(channelHandlerContext , (FullHttpRequest) o);
        }
        //webSocket 接入
        else if (o instanceof WebSocketFrame){
            logger.info("=========传输数据=========");
            handleWebSocketFrame(channelHandlerContext , (WebSocketFrame) o);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 如果连接采用的是HTTP请求，进行如下的处理
     * @param ctx
     * @param req
     */
    private void handleHttpRequest(ChannelHandlerContext ctx ,
                                   FullHttpRequest req){
        //如果HTTP解码失败，返回HTTP异常
        if (!req.decoderResult().isSuccess() ||
                (!"websocket".equals(req.headers().get("Upgrade")))){
            //webSocket的报文中Upgrade 如果的webSocket 的字样则代表是webSocket连接
            sendHttpResponse(ctx , req , new DefaultFullHttpResponse(HTTP_1_1,BAD_REQUEST));
            return;
        }

        //构造握手响应返回
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "ws://localhost:8080/webSocket",null,false);
        //建立握手请求
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null){
            //说明这个浏览器不支持webSocket
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }else {
            handshaker.handshake(ctx.channel() , req);
        }
    }

    /**
     * 如果进行WebSocket 连接，处理如下
     * @param ctx
     * @param frame
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx ,
                                      WebSocketFrame frame){
        //判断是否是关闭链路的指令
        if (frame instanceof CloseWebSocketFrame){
            handshaker.close(ctx.channel() , (CloseWebSocketFrame) frame.retain());
            return;
        }

        //判断是否是ping消息
        if (frame instanceof PingWebSocketFrame){
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //如果发送的不是文本
        if (!(frame instanceof TextWebSocketFrame)){
            throw new UnsupportedOperationException(
                    String.format("%s frame types not supported" , frame.getClass().getName()));
        }

        String request = ((TextWebSocketFrame) frame).text();
        ctx.channel().write(
                new TextWebSocketFrame(request + "，现在时刻：" +
                new Date().toString())
        );
    }


    private static void sendHttpResponse(ChannelHandlerContext ctx ,
                                         FullHttpRequest req , FullHttpResponse res){
        //状态不是200证明是不正常的HTTP请求
        if (res.getStatus().code() != HttpStatus.OK.value()){
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res , res.content().readableBytes());
        }

        ChannelFuture future = ctx.channel().writeAndFlush(res);

        //如果是非keep-Alive，关闭连接
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
