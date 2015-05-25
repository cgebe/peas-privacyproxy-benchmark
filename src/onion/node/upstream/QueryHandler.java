package onion.node.upstream;

import java.util.Random;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.apache.commons.codec.binary.Base64;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASMessage;

public class QueryHandler extends SimpleChannelInboundHandler<PEASMessage> {
	
	private NodeChannelInitializer initializer;

	public QueryHandler(NodeChannelInitializer initializer) {
		this.initializer = initializer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASMessage obj) throws Exception {
		if (obj.getHeader().getForward() == null) {
			if (obj.getHeader().getCommand().equals("QUERY")) {

				PEASHeader header = new PEASHeader();
				header.setCommand("RESPONSE");
				header.setStatus("100");
				header.setIssuer("NULL");
				header.setProtocol("HTTP");
				
				// body of forwarded msg
				byte[] content = initializer.getAESdecipher().doFinal(obj.getBody().getContent().array());
				System.out.println("c: " + new String(content));
				
				// simulating request to search engine here
				// normally open new socket to search engine/ make request
				int size = 8000;
				byte[] b = new byte[size];
				new Random().nextBytes(b);
				byte[] enc = initializer.getAEScipher().doFinal(b);
				
				header.setContentLength(enc.length);
				PEASBody body = new PEASBody(enc);
				
				// send response back
	            ChannelFuture f = ctx.writeAndFlush(new PEASMessage(header, body));
	            
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
				
			} else {
				ctx.fireChannelRead(obj);
			}  
		} else {
			ctx.fireChannelRead(obj);
		}
	}
}