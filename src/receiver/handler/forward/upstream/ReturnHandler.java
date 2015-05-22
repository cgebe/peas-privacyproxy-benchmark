package receiver.handler.forward.upstream;

import protocol.PEASObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ReturnHandler extends SimpleChannelInboundHandler<PEASObject> {

	private final Channel inboundChannel;
	private PEASObject obj;

    public ReturnHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
        //this.obj = toSend;
    }
    
    @Override public void channelActive(ChannelHandlerContext ctx) {
    	/*
    	ChannelFuture f = ctx.writeAndFlush(obj);

        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                	System.out.println("successful forward");
                } else {
                    System.out.println("failed forward");
                }
            }
        });
        */
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASObject toReturn) throws Exception {
        ChannelFuture f = inboundChannel.writeAndFlush(toReturn);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                	System.out.println("successful return");
                } else {
                	System.out.println("failed return");
                    future.channel().close();
                }
            }
        });
	}


}
