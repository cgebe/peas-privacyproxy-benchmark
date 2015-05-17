package onion.node.upstream;

import onion.node.forward.upstream.ForwardChannelInitializer;

import org.apache.commons.codec.binary.Base64;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ForwardHandler extends SimpleChannelInboundHandler<PEASObject> {
	
	private NodeChannelInitializer initializer;

	public ForwardHandler(NodeChannelInitializer initializer) {
		this.initializer = initializer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASObject obj) throws Exception {
		if (obj.getHeader().getCommand().equals("QUERY")) {
			if (obj.getHeader().getForward() != null) {
				
				String[] forward = new String(initializer.getAESdecipher().doFinal(Base64.decodeBase64(obj.getHeader().getForward()))).split("_");
				
				PEASHeader header = new PEASHeader();
				// forward to next node
				if (forward.length > 1) {
					header.setForward(forward[1]);
				} else {
					header.setForward(null);
				}
				
				header.setCommand("QUERY");
				header.setIssuer(forward[0]);
				
				String[] address = forward[0].split(":");
				
				// body of forwarded msg
				byte[] dec = initializer.getAESdecipher().doFinal(obj.getBody().getBody().array());
				
				PEASBody body = new PEASBody(dec);
				obj.setBody(body);
				
				// open new socket to next node address[0] = hostname, address[1] = port
				Channel inboundChannel = ctx.channel();

		        // Start the connection attempt.
		        Bootstrap b = new Bootstrap();
		        b.group(inboundChannel.eventLoop())
		         .channel(ctx.channel().getClass())
		         .handler(new ForwardChannelInitializer(inboundChannel, obj, initializer.getAEScipher()));
		        
		        ChannelFuture f = b.connect(address[0], Integer.parseInt(address[1]));
		       
		        f.addListener(new ChannelFutureListener() {
		            @Override
		            public void operationComplete(ChannelFuture future) {
		                if (future.isSuccess()) {
		                	System.out.println("connected to issuer");
		                } else {
		                	// TODO: normally send peas response with status code that issuer is not available

		                    // Close the connection if the connection attempt has failed.
		                	System.out.println("not connected to issuer");
		                    inboundChannel.close();
		                }
		            }
		        });
			}
		}
	}
}