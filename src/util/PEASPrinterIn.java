package util;

import protocol.PEASMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PEASPrinterIn extends SimpleChannelInboundHandler<PEASMessage> {
	
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	cause.printStackTrace();
        ctx.close();
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASMessage obj) throws Exception {
		System.out.println();
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<< IN <<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println(obj.toString());
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<< IN <<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println();
		ctx.fireChannelRead(obj);
	}
    


}