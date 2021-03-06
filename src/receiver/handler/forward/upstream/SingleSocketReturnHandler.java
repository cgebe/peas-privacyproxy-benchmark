package receiver.handler.forward.upstream;

import protocol.PEASMessage;
import receiver.server.ReceiverServer;
import io.netty.channel.Channel;
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
		Channel ret = server.getClients().remove(toReturn.getHeader().getReceiverID());
		
        ChannelFuture f = ret.writeAndFlush(toReturn);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
            	//server.getClients().get(toReturn.getHeader().getReceiverID()).close();
            	//Channel ch = server.getClients().remove(toReturn.getHeader().getReceiverID());
                if (future.isSuccess()) {
                	//System.out.println("successful return");
                	ret.close();
                } else {
                	//System.out.println("failed return");
                	ret.close();
                }
                
            }
        });
	}



}
