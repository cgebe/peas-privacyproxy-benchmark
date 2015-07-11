package receiver.handler.upstream;

import java.util.Random;

import protocol.PEASMessage;
import receiver.handler.forward.upstream.ForwardChannelInitializer;
import receiver.handler.forward.upstream.SingleSocketForwardChannelInitializer;
import receiver.server.ReceiverServer;
import util.Config;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

public class ForwardHandler extends SimpleChannelInboundHandler<PEASMessage> {

	private ReceiverServer server;

	public ForwardHandler(ReceiverServer server) {
		this.server = server;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASMessage obj) throws Exception {
		Channel inboundChannel = ctx.channel();
		
		if (Config.getInstance().getValue("SINGLE_SOCKET").equals("on")) {
			// Generate id for this client/peas request
			String id = randomID(32);
			obj.getHeader().setReceiverID(id);
			server.getClients().put(id, inboundChannel);
			
			if (server.getIssuers().get(obj.getHeader().getIssuer()) == null) {
		        // Start the connection attempt.
		        Bootstrap b = new Bootstrap();
		        b.group(inboundChannel.eventLoop())
		         .channel(ctx.channel().getClass())
		         .handler(new SingleSocketForwardChannelInitializer(server));
		        
		        ChannelFuture f = b.connect(obj.getHeader().getIssuerAddress(), obj.getHeader().getIssuerPort());
	
		        Channel ch = f.channel();
		        server.getIssuers().put(obj.getHeader().getIssuer(), ch);
		        
		        f.addListener(new ChannelFutureListener() {
		            @Override
		            public void operationComplete(ChannelFuture future) {
		                if (future.isSuccess()) {
		                	System.out.println("connected to issuer");
		                	ChannelFuture f = ch.writeAndFlush(obj);
		                	
		                	f.addListener(new ChannelFutureListener() {
		    		            @Override
		    		            public void operationComplete(ChannelFuture future) {
		    		                if (future.isSuccess()) {

		    		                } else {
		    		                	future.cause().printStackTrace();
		    		                	inboundChannel.close();
		    		                }
		    		            }
		    		        });
		                } else {
		                	// TODO: normally send peas response with status code that issuer is not available
	
		                    // Close the connection if the connection attempt has failed.
		                	System.out.println("not connected to issuerr");
		                    inboundChannel.close();
		                }
		            }
		        });
			} else {
				server.getIssuers().get(obj.getHeader().getIssuer()).writeAndFlush(obj);
			}
			
		} else {
	        // Start the connection attempt.
	        Bootstrap b = new Bootstrap();
	        b.group(inboundChannel.eventLoop())
	         .channel(ctx.channel().getClass())
	         .handler(new ForwardChannelInitializer(inboundChannel))
	         .option(ChannelOption.CONNECT_TIMEOUT_MILLIS , 500);
	        
	        ChannelFuture f = b.connect(obj.getHeader().getIssuerAddress(), obj.getHeader().getIssuerPort());
	        Channel ch = f.channel();
	        
	        f.addListener(new ChannelFutureListener() {
	            @Override
	            public void operationComplete(ChannelFuture future) {
	                if (future.isSuccess()) {
	                	System.out.println("connected to issuer");
	                	ChannelFuture f = ch.writeAndFlush(obj);
	                	
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
	                	future.cause().printStackTrace();
	                	// TODO: normally send peas response with status code that issuer is not available

	                    // Close the connection if the connection attempt has failed.
	                	System.out.println("not connected to issuer");
	                    inboundChannel.close();
	                }
	            }
	        });
		}
        
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        //closeOnFlush(ctx.channel());
    }
    

	static final String AB = "0123456789abcdefghijklmnopqrstuvwxyz";							
	static Random rnd = new Random();
	
	private String randomID(int length) 
	{
	   StringBuilder sb = new StringBuilder(length);
	   for( int i = 0; i < length; i++ ) 
	      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
	   return sb.toString();
	}



}
