package onion.node.upstream;

import java.util.Random;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.apache.commons.codec.binary.Base64;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASObject;
import protocol.PEASResponse;

public class QueryHandler extends SimpleChannelInboundHandler<PEASObject> {
	
	private NodeChannelInitializer initializer;

	public QueryHandler(NodeChannelInitializer initializer) {
		this.initializer = initializer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASObject obj) throws Exception {
		if (obj.getHeader().getCommand().equals("QUERY")) {
			if (obj.getHeader().getForward() == null) {
				

				PEASHeader header = new PEASHeader();
				header.setCommand("RESPONSE");
				header.setStatus("100");
				header.setIssuer("NULL");
				header.setProtocol("HTTP");
				
				// body of forwarded msg
				byte[] content = initializer.getAESdecipher().doFinal(obj.getBody().getBody().array());
				System.out.println("c: " + new String(content));
				
				// simulating request to search engine here
				// normally open new socket to search engine/ make request
				int size = 8000;
				byte[] b = new byte[size];
				new Random().nextBytes(b);
				b = initializer.getAEScipher().doFinal(b);
				PEASBody body = new PEASBody(b);
				
				PEASResponse res = new PEASResponse(header, body);
				
				// send response back
	            ChannelFuture f = ctx.writeAndFlush(res);
	            
	            f.addListener(new ChannelFutureListener() {
	                @Override
	                public void operationComplete(ChannelFuture future) {
	                    if (future.isSuccess()) {
	                    	System.out.println("return query successful");
	                    } else {
	                        System.out.println("return query failed");
	                        future.channel().close();
	                    }
	                }
	            });
				
			}
		}
	}
}