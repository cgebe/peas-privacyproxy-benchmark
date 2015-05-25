package receiver.handler.forward.upstream;

import protocol.PEASMessage;
import receiver.server.ReceiverServer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SingleSocketReturnHandler extends SimpleChannelInboundHandler<PEASMessage> {

	private ReceiverServer server;

    public SingleSocketReturnHandler(ReceiverServer server) {
        this.server = server;
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASMessage toReturn) throws Exception {
        ChannelFuture f = server.getClients().get(toReturn.getHeader().getReceiverID()).writeAndFlush(toReturn);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
            	future.channel().close();
            	//server.getClients().get(toReturn.getHeader().getReceiverID()).close();
            	server.getClients().remove(toReturn.getHeader().getReceiverID());
                if (future.isSuccess()) {
                	System.out.println("successful return");
                } else {
                	System.out.println("failed return");
                    
                }
            }
        });
	}


}
