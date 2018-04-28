package com.netty.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 *	丢弃任何进入的数据
 *	1.NioEventLoopGroup是用来处理I/O操作的多线程事件循环器。在这个例子中实现了一个服务端的应用，
 *	因此会有2个NioEventLoopGroup会被使用。boss用来接收进来的连接。worker用来处理已经被接收的连接
 *	2.ServerBootstrap 是一个启动NIO服务的辅助启动类。你可以在这个服务中直接使用Channel，但是
 *	这会是一个复杂的处理过程，在很多情况下你并不需要这样做
 *
 *	3.这里制定使用NioServerSocketChannel类来举例说明一个新的Channel如何接收进来的连接。
 *
 *	4.这里的事件处理类经常会被用来处理一个最近的已经接收的 Channel。ChannelInitializer 是一个特殊的
 *	处理类，他的目的是帮助使用者配置一个新的 Channel。也许你想通过增加一些处理类比如DiscardServerHandler 
 *	来配置一个新的 Channel 或者其对应的ChannelPipeline 来实现你的网络程序。当你的程序变的复杂时，可能你会
 *	增加更多的处理类到 pipline 上，然后提取这些匿名类到最顶层的类上。
 *
 *	5.你可以设置这里指定的 Channel 实现的配置参数。我们正在写一个TCP/IP 的服务端，因此我们被允许设置 socket 
 *	的参数选项比如tcpNoDelay 和 keepAlive。请参考 ChannelOption 和详细的 ChannelConfig 实现的接口文档以此可以
 *	对ChannelOption 的有一个大概的认识。
 *
 *	6.你关注过 option() 和 childOption() 吗？option() 是提供给NioServerSocketChannel 用来接收进来的连接。
 *	childOption() 是提供给由父管道 ServerChannel 接收到的连接，在这个例子中也是 NioServerSocketChannel。
 *
 *	7.我们继续，剩下的就是绑定端口然后启动服务。这里我们在机器上绑定了机器所有网卡上的 8080 端口。当然现在你
 *	可以多次调用 bind() 方法(基于不同绑定地址)。
 *
 */
public class DiscardServer {
	private int port;
	
	public DiscardServer(int port) {
		this.port = port;
	}
	
	public void run() throws Exception {
		EventLoopGroup bossGroup =new NioEventLoopGroup();	//1
		EventLoopGroup workerGroup =new NioEventLoopGroup();
		try {
			ServerBootstrap b =new ServerBootstrap();	//2
			b.group(bossGroup,workerGroup)
				.channel(NioServerSocketChannel.class)	//3
				.childHandler(new ChannelInitializer<SocketChannel>() {	//4
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new DiscardServerHandler());
					}
				})
				.option(ChannelOption.SO_BACKLOG, 128)				//5
				.childOption(ChannelOption.SO_KEEPALIVE, true);		//6
			
			//绑定端口，解释接收进来的连接
			ChannelFuture f =b.bind(port).sync();					//7
			
			
			//在这个例子中，这不会发生，但是 可以优雅的关闭服务器
			f.channel().closeFuture().sync();//等待服务器  socket关闭  主线程此时状态为wait()
			
		}finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception {
		int port;
		if(args.length > 0) {
			port = Integer.parseInt(args[0]);
		}else {
			port = 8082;
		}
		new DiscardServer(port).run();
	}
	
}
