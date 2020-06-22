package NettyJieba.SocketServer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class JiebaClient {
    private String host;
    private int port;

    public JiebaClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run(String msg) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new JiebaClientnitializer());
                    /*.handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 添加Decoder 保证数据完整
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()),new StringDecoder(), new StringEncoder(), new JiebaClientHandler());
                        }
                    });*/
            Channel channel = bootstrap.connect(host, port).sync().channel();
            ChannelFuture f = channel.writeAndFlush(msg+"\r\n");
            // 等待连接关闭
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws Exception {
        String value = "再打开多个浏览器页面实现多个 客户端访问";
        new JiebaClient("localhost", 8282).run(value);
    }
}
