package receiver.handler.upstream;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class StringPrinter extends SimpleChannelInboundHandler<String> {
	
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	cause.printStackTrace();
        ctx.close();
    }

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, String arg1) throws Exception {
		System.out.println(arg1);
	}
    


}