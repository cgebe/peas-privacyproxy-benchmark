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

    private volatile Channel outboundChannel;

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // was able to flush out data, start to read the next chunk
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //if (outboundChannel != null) {
           // closeOnFlush(outboundChannel);
        //}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        //closeOnFlush(ctx.channel());
    }

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
         .handler(new ForwardChannelInitializer(inboundChannel, obj))
         .option(ChannelOption.AUTO_READ, false);
        
        b.connect(obj.getHeader().getIssuerAddress(), obj.getHeader().getIssuerPort());
        
        /*
        outboundChannel = f.channel();
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                	//outboundChannel.writeAndFlush(obj);
                } else {
                	// TODO: normally send peas response with status code that issuer is not available
                	
                    // Close the connection if the connection attempt has failed.
                    inboundChannel.close();
                }
            }
        });
        */
	}



}
