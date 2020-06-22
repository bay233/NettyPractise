package NettyTest.Test4;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 1.扩展 ChannelInitializer
 *
 * 2.添加 ChannelHandler　到 ChannelPipeline
 *
 * initChannel() 方法设置 ChannelPipeline 中所有新注册的 Channel,安装所有需要的　 ChannelHandler。
 */
public class WebsocketChatServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Htpp 编码器
        pipeline.addLast(new HttpServerCodec());
        // object 聚合器
        pipeline.addLast(new HttpObjectAggregator(64*1024));
        // 分块写入Handler
        pipeline.addLast(new ChunkedWriteHandler());
        // 处理FullHttpRequest  只处理以 "/ws" 结尾的请求
        pipeline.addLast(new HttpRequestHandler("/ws"));
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        // 对请求 "/ws" 处理
        pipeline.addLast(new TextWebSocketFrameHandler());
    }
}
