package receiver.handler.forward.upstream;

import protocol.PEASObject;
import util.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ReturnHandler extends SimpleChannelInboundHandler<PEASObject> {

	private final Channel inboundChannel;
	private PEASObject obj;

    public ReturnHandler(Channel inboundChannel, PEASObject toSend) {
        this.inboundChannel = inboundChannel;
        this.obj = toSend;
    }
    
    @Override public void channelActive(ChannelHandlerContext ctx) {
    	ChannelFuture f = ctx.writeAndFlush(obj);

        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
            	//future.channel().close();
                if (future.isSuccess()) {
                	//future.channel().writeAndFlush(msg);
                	System.out.println("successful forward");
                    // ctx.channel().read();
                } else {
                    //future.channel().close();
                    System.out.println("failed forward");
                }
            }
        });
        
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        //ForwardHandler.closeOnFlush(ctx.channel());
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASObject toReturn) throws Exception {
        ChannelFuture f = inboundChannel.writeAndFlush(toReturn);
        
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
            	future.channel().close();
                if (future.isSuccess()) {
                	System.out.println("successful return");
                   // ctx.channel().read();
                } else {
                	System.out.println("failed return");
                    future.channel().close();
                }
            }
        });
        
	}


}
