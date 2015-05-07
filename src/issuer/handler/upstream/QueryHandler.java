package issuer.handler.upstream;

import protocol.PEASObject;
import server.dao.Query;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class QueryHandler extends SimpleChannelInboundHandler<PEASObject> {

	@Override
	public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, PEASObject obj) throws Exception {
		if (obj.getHeader().getCommand().equals("QUERY")) {
			System.out.println("query received");
			// TODO: decrypt query field and body, and dispatch http or forward to another issuer
		}
	}

}
