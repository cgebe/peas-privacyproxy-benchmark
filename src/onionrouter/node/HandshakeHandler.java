package onionrouter.node;

import java.nio.file.Files;
import java.nio.file.Paths;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASObject;
import protocol.PEASResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HandshakeHandler extends SimpleChannelInboundHandler<PEASObject> {

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, PEASObject arg1)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	

}
