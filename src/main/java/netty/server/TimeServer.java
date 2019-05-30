package netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author lancelord-lajiao
 * @version 1.0
 * @title
 * @description
 * @createDate 2019/5/21 22:40
 */
public class TimeServer {
    public void bin(int port)throws Exception{
        //配置服务端的线程组 一个用于服务端接受客户端的连接，一个进行socketchannel的网络读写
        EventLoopGroup bossGroup = new NioEventLoopGroup() ;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //netty用于启动nio服务端的辅助启动类
            ServerBootstrap b = new ServerBootstrap();
            //设置channel类型对应jdk的noiServerSockect 并配置tcp参数
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024).
                    //绑定io处理类
                    childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new TimeServerHandler());
                        }
                    });
            //绑定端口，同步等待成功
            ChannelFuture channelFuture = b.bind(port).sync();
            //等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        }finally {
            //退出释放线程资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }
}
