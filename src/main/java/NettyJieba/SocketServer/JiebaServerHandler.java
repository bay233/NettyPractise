package NettyJieba.SocketServer;

import com.huaban.analysis.jieba.JiebaSegmenter;
import io.netty.channel.*;

import java.util.List;

public class JiebaServerHandler extends SimpleChannelInboundHandler<String> {
    private JiebaSegmenter segmenter = new JiebaSegmenter();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("请求的数据为："+msg);
        List<String> StringList = segmenter.sentenceProcess(msg);
        String result = String.join(" ",StringList);
        System.out.println("返回的数据为："+result);
        ctx.writeAndFlush(result+"\r\n");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("SimpleJiebaClient:"+incoming.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
