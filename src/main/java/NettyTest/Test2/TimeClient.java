package NettyTest.Test2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TimeClient {

    public static void main(String[] args) throws InterruptedException {
        int port = 8080;
        if (args.length > 0){
            port = Integer.parseInt(args[0]);
        }

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 添加Decoder 保证数据完整
                    ch.pipeline().addLast(new TimeDecoder(), new TimeClientHandler());
                }
            });

            // 启动客服端
            ChannelFuture f = b.connect("localhost", port).sync();

            // 等待连接关闭
            f.channel().closeFuture().sync();

        }finally{
            // 关闭构建的所有的 EventLoopGroup  返回一个Future对象通知
            workerGroup.shutdownGracefully();
        }


    }
}
