package com.netty.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 *	处理服务端 channel
 * 	@author DELL
 *	1.ChannelInboundHandler提供了许多时间处理的接口方法，我们可以覆盖这些方法。
 *	只需要集成ChannelInboundHandlerAdapter类而不是自己去实现接口方法。
 *	2.覆盖了channelRead()事件处理方法。每当从客户端收到新的数据时，都被调用。
 *	3.为了实现DISCARD协议，处理器不得不忽略所有接收到的消息。ByteBuf是一个引用计数对象，
 *	这个对象必须显示地调用release()方法来释放。请记住处理器的职责是释放所有传递到处理器的引用计数对象
 *	4.exceptionCaught()事件处理方法是在出现 Throwable 对象才会被调用。
 *	
 *	
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {//1
//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {//2
//		//默默地丢弃收到的数据
//		((ByteBuf)msg).release();//3
//	}
//	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {//2
//		ByteBuf in =(ByteBuf) msg;
//		try {
//			/*while(in.isReadable()) { // (1) 低效的循环
//				System.out.println((char)in.readByte());
//				System.out.flush();
//			}*/
//			System.out.println(in.toString(io.netty.util.CharsetUtil.US_ASCII));
//		}finally {
//			ReferenceCountUtil.release(msg); // (2)或者，可以在这里调用in.release()
//		}
//	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		/*
		 * ChannelHandlerContext 对象提供了许多操作，使你能够触发各种各样的 I/O 事件和操作。
		 * 这里我们调用了 write(Object) 方法来逐字地把接受到的消息写入。请注意不同于 DISCARD
		 *  的例子我们并没有释放接受到的消息，这是因为当写入的时候 Netty 已经帮我们释放了。
		 */
		ctx.write(msg);
		/*
		 * ctx.write(Object) 方法不会使消息写入到通道上，他被缓冲在了内部，你需要调用 ctx.flush()
		 * 方法来把缓冲区中数据强行输出。或者你可以用更简洁的 cxt.writeAndFlush(msg) 以达到同样的目的。
		 */
		ctx.flush();
	}

	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {//4
		//当出现异常就关闭连接
		cause.printStackTrace();
		ctx.close();
	}
		
}
