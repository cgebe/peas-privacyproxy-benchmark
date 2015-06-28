package onion.node.upstream;


import onion.node.forward.upstream.ForwardChannelInitializer;

import org.apache.commons.codec.binary.Base64;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HandshakeForwardHandler extends SimpleChannelInboundHandler<PEASMessage> {
	
	private NodeChannelState channelState;

	public HandshakeForwardHandler(NodeChannelState channelState) {
		this.channelState = channelState;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASMessage obj) throws Exception {
		if (obj.getHeader().getCommand().equals("HANDSHAKE")) {
			if (obj.getHeader().getForward() != null) {
				String[] forward = new String(channelState.getAESdecipher().doFinal(Base64.decodeBase64(obj.getHeader().getForward()))).split("_");
				
				PEASHeader header = new PEASHeader();
				// forward to next node
				if (forward.length > 1) {
					header.setForward(forward[1]);
				} else {
					header.setForward(null);
				}
				
				header.setCommand("HANDSHAKE");
				header.setIssuer(forward[0]);
				header.setStatus("300");
				
				String[] address = forward[0].split(":");
				
				// body of forwarded msg
				byte[] dec = channelState.getAESdecipher().doFinal(obj.getBody().getContent().array());
				header.setContentLength(dec.length);
				PEASBody body = new PEASBody(dec);
				
				// open new socket to next node address[0] = hostname, address[1] = port
				Channel inboundChannel = ctx.channel();

				//initializer.forward(address[0], Integer.parseInt(address[1]), new PEASRequest(header, body), inboundChannel, ctx);
				// Start the connection attempt.
		        Bootstrap b = new Bootstrap();
		        b.group(inboundChannel.eventLoop())
		         .channel(ctx.channel().getClass())
		         .handler(new ForwardChannelInitializer(inboundChannel, channelState));
		        
		        ChannelFuture f = b.connect(address[0], Integer.parseInt(address[1]));
		       
		        f.addListener(new ChannelFutureListener() {
		            @Override
		            public void operationComplete(ChannelFuture future) {
		                if (future.isSuccess()) {
		                	System.out.println("connected to next node");
		                } else {
		                	// TODO: normally send peas response with status code that issuer is not available

		                    // Close the connection if the connection attempt has failed.
		                	System.out.println("not connected to next node");
		                    inboundChannel.close();
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
