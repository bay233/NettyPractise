package NettyTest.Test2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 服务器实现类
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    //channelActive() 方法将会在连接被建立并且准备进行通信时被调用
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        /*final ByteBuf time = ctx.alloc().buffer(4);
        time.writeInt( (int) (System.currentTimeMillis() / 1000L + 2208988800L));
        ChannelFuture f = ctx.writeAndFlush(time);*/
        /*f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                assert f == future;
                ctx.close();
            }
        });
        f.addListener(ChannelFutureListener.CLOSE);*/

        //方法二 使用POJO类
        ChannelFuture f = ctx.writeAndFlush(new UnixTime());
        f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
