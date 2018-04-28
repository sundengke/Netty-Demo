package com.netty.time;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 *	这个部分被实现的协议是TIME协议。
 *	在这个例子中，你会学习到如何构建和发送一个消息，然后在完成时关闭连接。
 *
 *	因为我们将会忽略任何接收的数据，而只是在连接被创建发送一个消息，所以这次我们不能使用
 *	channelRead()方法了，代替的是，我们需要覆盖channelActive()方法
 *
 *	1.channelActive()方法将会在连接被建立并且准备进行通信时被调用。因此在这个方法里完成
 *	一个代表当前时间的32位整数消息的构建工作。
 *	2.为了发送一个新的消息，我们需要分配一个包含这个消息的新的缓冲。这个例子中我们需要一个
 *	至少4个字节的ByteBuf。通过ChannelHandlerContext.alloc()得到一个当前的ByteBufAllocator，然后
 *	分配一个新的缓冲。
 *	
 *	
 *	
 *	
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter{
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {	//1
		final ByteBuf time =ctx.alloc().buffer(4);							//2
		time.writeInt((int)(System.currentTimeMillis()/1000L+2208988800L));
		
		final ChannelFuture f =ctx.writeAndFlush(time);		//3
		f.addListener(new ChannelFutureListener() {

			public void operationComplete(ChannelFuture future) throws Exception {
				assert f ==future;
				//ctx.close();
			}
		});	//4
		//f.addListener(ChannelFutureListener.CLOSE);
	}
	/**
	 * 3.和往常一样我们需要编写一个构建好的消息。但是等一等，flip 在哪？难道我们使用 NIO 发送消息时
	 * 不是调用 java.nio.ByteBuffer.flip() 吗？ByteBuf 之所以没有这个方法因为有两个指针，一个对应
	 * 读操作一个对应写操作。当你向 ByteBuf 里写入数据的时候写指针的索引就会增加，同时读指针的索引没有变化。
	 * 读指针索引和写指针索引分别代表了消息的开始和结束。
	 * 
	 * 比较起来，NIO 缓冲并没有提供一种简洁的方式来计算出消息内容的开始和结尾，除非你调用 flip 方法。
	 * 当你忘记调用 flip 方法而引起没有数据或者错误数据被发送时，你会陷入困境。这样的一个错误不会发生在 Netty 上，
	 * 因为我们对于不同的操作类型有不同的指针。你会发现这样的使用方法会让你过程变得更加的容易，因为你已经习惯一种没有使用 flip 的方式。
	 * 
	 * 另外一个点需要注意的是 ChannelHandlerContext.write() (和 writeAndFlush() )方法会返回一个 ChannelFuture 对象，
	 * 一个 ChannelFuture 代表了一个还没有发生的 I/O 操作。这意味着任何一个请求操作都不会马上被执行，因为在 Netty 里所有的操作都是异步的。
	 * 举个例子下面的代码中在消息被发送之前可能会先关闭连接。
	 * 
	 * Channel ch = ...;
     * ch.writeAndFlush(message);
     * ch.close();
     * 因此你需要在 write() 方法返回的 ChannelFuture 完成后调用 close() 方法，然后当他的写操作已经完成他会通知他的监听者。
     * 请注意,close() 方法也可能不会立马关闭，他也会返回一个ChannelFuture。
     * 
     * 4.请求完成后是如何通知的。只需要在返回的ChannelFuture上增加一个ChannelFutureListener.
     * 这里可以使用简单的预定义鉴定器代码 :  f.addListener(ChannelFutureListener.CLOSE);
	 */
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
		
	}
}
