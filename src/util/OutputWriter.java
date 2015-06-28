package util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class OutputWriter extends ChannelOutboundHandlerAdapter {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		ByteBuf buf = (ByteBuf) msg;
		Observer.getInstance().setOutput(Observer.getInstance().getOutput() + buf.capacity());
		ctx.write(msg);
	}

}