package com.netty.time;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 *  ** 在Netty中，编写服务端和客户端最大的并且唯一的不同：是使用了不同的BootStrap和Channel的实现
 * 
 *	如何确保服务端是正常工作的，
 *	学习怎么用Netty编写一个客户端
 *
 *		1.BootStrap 和 ServerBootstrap 类似,不过他是对非服务端的 channel 而言，
 *比如客户端或者无连接传输模式的 channel。
 *
 *		2.如果你只指定了一个 EventLoopGroup，那他就会即作为一个 boss group ，也会
 *作为一个 workder group，尽管客户端不需要使用到 boss worker 。
 *
 *		3.代替NioServerSocketChannel的是NioSocketChannel,这个类在客户端channel 被创建时使用。
 *
 *		4.不像在使用 ServerBootstrap 时需要用 childOption() 方法，因为客户端的 SocketChannel 没有父亲。
 *
 *		5.我们用 connect() 方法代替了 bind() 方法。
 *
 */
public class TimeClient {
	public static void main(String[] args) throws Exception {
		String host =args[0];
		int port =Integer.parseInt(args[1]);
		EventLoopGroup workerGroup =new NioEventLoopGroup();
		
		try {
			Bootstrap b =new Bootstrap();		//1		与服务端的差异
			b.group(workerGroup);				//2
			b.channel(NioSocketChannel.class);	//3		与服务端的差异    
			b.option(ChannelOption.SO_KEEPALIVE, true);	//4服务端需要使用childOption()方法，因为客户端的ScoketChannel没有父类
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new TimeServerHandler());
				}
			});
			
			//启动客户端
			ChannelFuture f = b.connect(host,port).sync(); //5 也可以用bind();
			
			//等待Socket连接关闭 , 此时线程的状态为wait
			f.channel().closeFuture().sync();
		}finally {
			workerGroup.shutdownGracefully();
		}
	}
	/*处理器有时候会因为抛出 IndexOutOfBoundsException 而拒绝工作。
	 */
}
