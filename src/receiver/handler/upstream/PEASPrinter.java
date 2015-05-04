package receiver.handler.upstream;

import protocol.PEASObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PEASPrinter extends SimpleChannelInboundHandler<PEASObject> {
	
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	cause.printStackTrace();
        ctx.close();
    }

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, PEASObject obj) throws Exception {
		System.out.println(obj.toString());
	}
    


}