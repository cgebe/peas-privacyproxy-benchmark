package onion.node.upstream;

import onion.node.forward.upstream.ForwardChannelInitializer;
import onion.node.forward.upstream.ReturnHandler;

import org.apache.commons.codec.binary.Base64;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASObject;
import protocol.PEASRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

public class ForwardHandler2 extends SimpleChannelInboundHandler<PEASObject> {
	
	private NodeChannelInitializer initializer;
	private Channel outboundChannel;

	public ForwardHandler2(NodeChannelInitializer initializer) {
		this.initializer = initializer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASObject obj) throws Exception {
		if (obj.getHeader().getForward() != null) {
			String[] forward = new String(initializer.getAESdecipher().doFinal(Base64.decodeBase64(obj.getHeader().getForward()))).split("_");
			PEASHeader header = new PEASHeader();
			// forward to next node
			if (forward.length > 1) {
				System.out.println("0: " + forward[0]);
				System.out.println("1: " + forward[1]);
				header.setForward(forward[1]);
			} else {
				System.out.println("0: " + forward[0]);
				header.setForward(null);
			}
			
			header.setCommand(obj.getHeader().getCommand());
			header.setIssuer("ONION");
			String[] address = forward[0].split(":");
			
			// body of forwarded msg
			byte[] dec = initializer.getAESdecipher().doFinal(obj.getBody().getContent().array());
			header.setContentLength(dec.length);
			PEASBody body = new PEASBody(dec);
			
			// open new socket to next node address[0] = hostname, address[1] = port
			Channel inboundChannel = ctx.channel();
			
			if (outboundChannel == null) {
		        // Start the connection attempt.
		        Bootstrap b = new Bootstrap();
		        b.group(inboundChannel.eventLoop())
		         .channel(ctx.channel().getClass())
		         .handler(new ForwardChannelInitializer(inboundChannel, initializer.getAEScipher()));
		        
		        ChannelFuture f = b.connect(address[0], Integer.parseInt(address[1]));
	
		        outboundChannel = f.channel();
		        
		        f.addListener(new ChannelFutureListener() {
		            @Override
		            public void operationComplete(ChannelFuture future) {
		                if (future.isSuccess()) {

		                	ChannelFuture f = outboundChannel.writeAndFlush(new PEASRequest(header, body));
		                	
		                	f.addListener(new ChannelFutureListener() {
		    		            @Override
		    		            public void operationComplete(ChannelFuture future) {
		    		                if (future.isSuccess()) {

		    		                } else {
		    		                	future.cause().printStackTrace();
		    		                }
		    		            }
		    		        });
		                } else {
		                	// TODO: normally send peas response with status code that issuer is not available
	
		                    // Close the connection if the connection attempt has failed.
		                	System.out.println("not connected to next node");
		                    inboundChannel.close();
		                }
		            }
		        });
			} else {
				outboundChannel.writeAndFlush(new PEASRequest(header, body));
			}
		} else {
			ctx.fireChannelRead(obj);
		}

	}
}