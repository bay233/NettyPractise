package NettyJieba.WebServer;

import NettyTest.Test4.TextWebSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebJiebaServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Htpp 编码器
        pipeline.addLast(new HttpServerCodec());
        // object 聚合器
        pipeline.addLast(new HttpObjectAggregator(64*1024));
        // 分块写入Handler
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler("/jb"));
        // 对请求 "/ws" 处理
        pipeline.addLast(new WebJiebaServerHandler());
    }
}
