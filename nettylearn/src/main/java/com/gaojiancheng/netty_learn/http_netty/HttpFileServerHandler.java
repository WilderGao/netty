package com.gaojiancheng.netty_learn.http_netty;

import com.sun.org.apache.regexp.internal.RE;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

/**
 * @author:Wilder Gao
 * @time:2018/2/23
 * @Discription：
 */
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private String url;
    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\\\.]*");
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    public HttpFileServerHandler(String url){
        this.url = url;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        if (!fullHttpRequest.decoderResult().isSuccess()){
            sendError(channelHandlerContext , HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if (fullHttpRequest.method() != HttpMethod.GET){
            sendError(channelHandlerContext , HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        final String uri = fullHttpRequest.uri();
        final String path = sanitizeUri(uri);
        if (path == null){
            sendError(channelHandlerContext , HttpResponseStatus.FORBIDDEN);
            return;
        }

        File file = new File(path);
        if (file.isHidden() || !file.exists()){
            sendError(channelHandlerContext , HttpResponseStatus.NOT_FOUND);
            return;
        }
        if (file.isDirectory()){
            if (uri.endsWith("/")){
                sendListing(channelHandlerContext , file);
            }else {
                sendRedirect(channelHandlerContext , uri+"/");
            }
            return;
        }
        //不是目录也不是文件
        if (!file.isFile()){
            sendError(channelHandlerContext , HttpResponseStatus.FORBIDDEN);
            return;
        }

        RandomAccessFile randomAccessFile = null;
        randomAccessFile = new RandomAccessFile(file , "r");

        long fileLength = randomAccessFile.length();
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1 , HttpResponseStatus.OK);
        HttpUtil.setContentLength(response , fileLength);
        setContentTypeHeader(response , file);

        if (HttpUtil.isKeepAlive(fullHttpRequest)){
            response.headers().set(HttpHeaderNames.CONNECTION , HttpHeaderValues.KEEP_ALIVE);
        }

        channelHandlerContext.write(response);
        ChannelFuture sendFileFuture = channelHandlerContext.write(new ChunkedFile(
                randomAccessFile , 0 , fileLength , 8192) ,
                channelHandlerContext.newProgressivePromise());
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long progress, long total) throws Exception {
                if (total < 0){
                    System.err.println("Transfer progress "+ progress);
                }else {
                    System.err.println("Transfer progress "+ progress+"/"+total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture channelProgressiveFuture) throws Exception {
                System.out.println("Transfer complete......");
            }
        });

        ChannelFuture lastContentFuture = channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (HttpUtil.isKeepAlive(fullHttpRequest))
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive())
            sendError(ctx , HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 对URL进行清洗
     * @param uri
     * @return
     */
    private String sanitizeUri(String uri){
        try {
            uri = URLDecoder.decode(uri , "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("===== utf-8 解码URL 出错 =====");
            try {
                uri = URLDecoder.decode(uri , "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
                throw new Error();
            }
        }
        System.out.println("uri 打印结果："+uri);
        System.out.println("url 打印结果："+url);
        if (!uri.startsWith(url))
            return null;
        if (!uri.startsWith("/"))
            return null;
        uri = uri.replace('/' , File.separatorChar);
        if (uri.contains(File.separator + '.')|| uri.contains('.' + File.separator)
                || uri.startsWith(".") || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches())
            return null;
        return System.getProperty("user.dir")+File.separator + uri;
    }


    /**
     * 正常情况，获取文件列表，并且以html页面展现出来
     * @param ctx
     * @param dir
     */
    private static void sendListing(ChannelHandlerContext ctx , File dir){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1 , HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE , "text/html;charset=UTF-8");
        //得到文件路径
        String dirPath = dir.getPath();
        StringBuilder buf = new StringBuilder();
        //构造网页html代码
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append("目录:");
        buf.append("</title></head><body>\r\n");

        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\" ../\")..</a></li>\r\n");

        for (File file : dir.listFiles()){
            if (!file.canRead() || file.isHidden()) {
                continue;
            }
            String name = file.getName();
            //名字不符合这个正则表达式
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }

            buf.append("<li>连接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");

        }
        buf.append("</ul></body></html>\r\n");

        ByteBuf byteBuf = Unpooled.copiedBuffer(buf , CharsetUtil.UTF_8);
        response.content().writeBytes(byteBuf);
        byteBuf.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 发送错误的情况
     * @param ctx
     * @param status
     */
    private static void sendError(ChannelHandlerContext ctx , HttpResponseStatus status){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1 , status ,
                Unpooled.copiedBuffer("Failure:"+status.toString()+"\r\n", CharsetUtil.UTF_8));
        //设置返回头的ContentType，也就是显示的类型
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html;charset=UTF-8");
        //返回内容后关闭连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 重新设置ContentType 请求头
     * @param response
     * @param file
     */
    private static void setContentTypeHeader(HttpResponse response , File file){
        MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE ,
                mimetypesFileTypeMap.getContentType(file.getPath()));
    }

    private static void sendRedirect(ChannelHandlerContext ctx , String newUri){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1 , HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.LOCATION , newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
