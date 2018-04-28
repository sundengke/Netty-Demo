package com.netty.time;

import java.sql.Date;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 *	定义一个时间客户端的处理器
 *	他应该从服务端接收一个32位的证书消息，把消息翻译成可读懂的合适。最后关闭连接。
 *	
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {
	private ByteBuf buf;
	
	/*
	 * ChannelHandler有2个生命周期的监听方法：handlerAdded()和handlerRemoved()。
	 * 可以完成任意初始化任务，只要不会被阻塞很长时间。
	 * 
	 */
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		buf =ctx.alloc().buffer(4);
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		buf.release();
		buf =null;
	}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//在TCP/IP中，Netty会把读到的数据放在BtyeBuf的数据结构中
		ByteBuf m=(ByteBuf) msg;
		//使用内部缓冲    **（1）
		buf.writeBytes(m); //所有接收的数据都被累积在buf变量中
		
		m.release();
		
		if(buf.readableBytes()>=4) {//处理器必须检查buf变量是否有足够的数据，这个例子是4个字节，饭后处理实际的业务员逻辑
									//否则，Netty会重复调用channelRead()当有更多数据到达直到4个字节的数据被积累
			long currentTimeMillis =(m.readUnsignedInt()-2208988800L)*100L;
			System.out.println(new Date(currentTimeMillis));
			ctx.close();
		}
//		try {
//			long currentTimeMillis =(m.readUnsignedInt()-2208988800L)*100L;
//			System.out.println(new Date(currentTimeMillis));
//			ctx.close();
//		}finally {
//			m.release();
//		}
	}
	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
	/* 这样看起来非常简单，并且和服务端的那个例子的代码也相差不多。
	 * 然而，处理器有时候会因为抛出 IndexOutOfBoundsException 而拒绝工作。
	 * 解决方法
	 * **（1）最简单的方案是构造一个内部的可积累的缓冲，直到4个字节全部接收到了内部缓冲。
	 * 
	 * 
	 * 
	 */
}
