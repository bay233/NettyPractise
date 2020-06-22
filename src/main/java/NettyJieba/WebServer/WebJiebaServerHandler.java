package NettyJieba.WebServer;

import com.huaban.analysis.jieba.JiebaSegmenter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.net.URLDecoder;
import java.util.List;

//TextWebSocketFrame
public class WebJiebaServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private JiebaSegmenter segmenter = new JiebaSegmenter();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        if (msg.uri().lastIndexOf("/jieba") > 0) {
            String uri = msg.uri();
            System.out.println("请求的地址为：" + uri);
            String value = uri.substring(1, msg.uri().lastIndexOf("/jieba"));
            String decode = URLDecoder.decode(value);
            System.out.println("解码后字符串：" + decode);

            List<String> StringList = segmenter.sentenceProcess(decode);
            String result = String.join(" ", StringList);

            System.out.println("返回的数据为：" + result);
            //byte[] bytes = result.getBytes(CharsetUtil.UTF_8);
            byte[] bytes = result.getBytes();
            ByteBuf buf = Unpooled.wrappedBuffer(bytes);
            DefaultHttpHeaders headers = new DefaultHttpHeaders();
            headers.add("content-type", "text/html; charset=UTF-8");
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf, headers, headers);
            ctx.writeAndFlush(response);
        } else {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            ctx.writeAndFlush(response);
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("SimpleJiebaClient:" + incoming.remoteAddress() + "异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
