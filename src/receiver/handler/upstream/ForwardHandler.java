package receiver.handler.upstream;

import protocol.PEASObject;
import receiver.handler.forward.upstream.ForwardChannelInitializer;
import util.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

public class ForwardHandler extends SimpleChannelInboundHandler<PEASObject> {


    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASObject obj) throws Exception {
		final Channel inboundChannel = ctx.channel();

        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
         .channel(ctx.channel().getClass())
         .handler(new ForwardChannelInitializer(inboundChannel, obj));
        
        ChannelFuture f = b.connect(obj.getHeader().getIssuerAddress(), obj.getHeader().getIssuerPort());
       
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                	//outboundChannel.writeAndFlush(obj);
                	System.out.println("connected to issuer");
                } else {
                	// TODO: normally send peas response with status code that issuer is not available
                	
                    // Close the connection if the connection attempt has failed.
                	System.out.println("not connected to issuer");
                    inboundChannel.close();
                }
            }
        });
        
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        //closeOnFlush(ctx.channel());
    }



}
