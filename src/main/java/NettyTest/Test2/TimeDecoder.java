package NettyTest.Test2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * netty 是以字节流的信息进行数据传输，不保证消息的完整性，所以需要自定义一个消息验证功能
 * 解码器  and  消息验证
 */
public class TimeDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4){
            return ;
        }
        //out.add(in.readBytes(4));
        // 方法二 使用POJO类
        out.add(new UnixTime(in.readUnsignedInt()));

    }
}
