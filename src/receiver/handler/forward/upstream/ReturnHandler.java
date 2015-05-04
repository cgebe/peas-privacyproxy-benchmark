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
    	ctx.writeAndFlush(obj);
        /*
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
            	//future.channel().close();
                if (future.isSuccess()) {
                	//future.channel().writeAndFlush(msg);
                	System.out.println("success write");
                    // ctx.channel().read();
                } else {
                    //future.channel().close();
                    System.out.println("failed write");
                }
            }
        });
        */
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        //ForwardHandler.closeOnFlush(ctx.channel());
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASObject toReturn) throws Exception {
        inboundChannel.writeAndFlush(toReturn);
        /*
        .addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
            	future.channel().close();
                if (future.isSuccess()) {
                	System.out.println("returned");
                   // ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
        */
	}


}
