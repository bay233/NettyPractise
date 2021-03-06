package NettyTest.Test4;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 扩展 SimpleChannelInboundHandler 用于处理 FullHttpRequest信息
 *
 * HttpRequestHandler 做了下面几件事，
 *
 * 如果该 HTTP 请求被发送到URI “/ws”，调用 FullHttpRequest 上的 retain()，
 * 并通过调用 fireChannelRead(msg) 转发到下一个 ChannelInboundHandler。
 * retain() 是必要的，因为 channelRead() 完成后，它会调用 FullHttpRequest 上的 release() 来释放其资源。
 * （请参考我们先前的 SimpleChannelInboundHandler 在第6章中讨论）
 *
 * 如果客户端发送的 HTTP 1.1 头是“Expect: 100-continue” ，将发送“100 Continue”的响应。
 *
 * 在 头被设置后，写一个 HttpResponse 返回给客户端。注意，这是不是 FullHttpResponse，唯一的反应的第一部分。
 * 此外，我们不使用 writeAndFlush() 在这里 - 这个是在最后完成。
 *
 * 如果没有加密也不压缩，要达到最大的效率可以是通过存储 index.html 的内容在一个 DefaultFileRegion 实现。
 * 这将利用零拷贝来执行传输。出于这个原因，我们检查，看看是否有一个 SslHandler 在 ChannelPipeline 中。
 * 另外，我们使用 ChunkedNioFile。
 *
 * 写 LastHttpContent 来标记响应的结束，并终止它
 *
 * 如果不要求 keepalive ，添加 ChannelFutureListener 到 ChannelFuture 对象的最后写入，并关闭连接。
 * 注意，这里我们调用 writeAndFlush() 来刷新所有以前写的信息。
 *
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    // ws请求的uri
    private String wsUri;
    private static File INDEX;

    static {
        // 获取resource 文件目录
        URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            // 获取html文件路径
            String path = location.toURI() + "WebsocketChatClient.html";
            path = !path.contains("file:") ? path : path.substring(6);
            INDEX = new File(path);
        }catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to locate WebsocketChatClient.html", e);
        }
    }

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //如果请求是 WebSocket 升级，递增引用计数器（保留）
        // 并且将它传递给在 ChannelPipeline 中的下个 ChannelInboundHandler
        if(wsUri.equalsIgnoreCase(request.getUri())){
            ctx.fireChannelRead(request.retain());
        }else{
            if (HttpHeaders.is100ContinueExpected(request)){
                send100Continue(ctx);
            }
            // 读取默认的 WebsocketChatClient.html 页面
            RandomAccessFile file = new RandomAccessFile(INDEX, "r");
            DefaultHttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");

            boolean keepAlive = HttpHeaders.isKeepAlive(request);

            // 判断 keepalive 是否在请求头里面
            if (keepAlive){
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,file.length());
                response.headers().set(HttpHeaders.Names.CONNECTION,HttpHeaders.Values.KEEP_ALIVE);
            }

            ctx.write(response);

            // 写 index.html 到客户端，
            // 判断 SslHandler 是否在 ChannelPipeline 来决定是使用 DefaultFileRegion 还是 ChunkedNioFile
            if (ctx.pipeline().get(SslHandler.class) == null){
                ctx.write(new DefaultFileRegion(file.getChannel(),0,file.length()));
            }else{
                ctx.write(new ChunkedNioFile(file.getChannel()));
            }
            // 写并刷新 LastHttpContent 到客户端，标记响应完成
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

            //如果 keepalive 没有要求，当写完成时，关闭 Channel
            if (!keepAlive){
                future.addListener(ChannelFutureListener.CLOSE);
            }

            file.close();
        }
    }


    private static void send100Continue(ChannelHandlerContext ctx){
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("Client:"+incoming.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
