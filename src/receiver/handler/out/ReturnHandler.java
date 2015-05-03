package receiver.handler.out;

import util.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ReturnHandler extends SimpleChannelInboundHandler<Message> {

	private final Channel inboundChannel;

    public ReturnHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        //ForwardHandler.closeOnFlush(ctx.channel());
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
            	future.channel().close();
                //if (future.isSuccess()) {
                   // ctx.channel().read();
               // } else {
                    future.channel().close();
                //}
            }
        });
	}


}
