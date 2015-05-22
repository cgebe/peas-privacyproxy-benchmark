package onion.node.upstream;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.crypto.Cipher;

import onion.node.forward.upstream.ForwardChannelInitializer;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

import protocol.PEASObject;

import com.squareup.crypto.rsa.NativeRSAEngine;

import receiver.handler.upstream.PEASPrinter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import codec.PEASDecoder;
import codec.PEASDecoder2;
import codec.PEASDecoder3;
import codec.PEASEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NodeChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private Channel forward = null;
	private ChannelPipeline pipeline;
	private AsymmetricBlockCipher RSAdecipher;
	private Cipher AEScipher;
	private Cipher AESdecipher;
	private Channel outboundChannel;
	
	public NodeChannelInitializer(AsymmetricKeyParameter key) {
        RSAdecipher = new PKCS1Encoding(new RSAEngine());
        RSAdecipher.init(false, key);
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		//pipeline.addLast("framer", new FixedLengthFrameDecoder(1));
		pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast("peasdecoder", new PEASDecoder3()); // upstream 1
        pipeline.addLast("peasencoder", new PEASEncoder()); // downstream 1
        pipeline.addLast("peasprinter", new PEASPrinter()); // upstream 2
        pipeline.addLast("handshakehandler", new HandshakeHandler(this)); // upstream 3
        //pipeline.addLast("handshakeforwarder", new HandshakeForwardHandler(this)); // upstream 4
        pipeline.addLast("queryforwarder", new ForwardHandler2(this)); // upstream 5
        //pipeline.addLast("queryhandler", new ForwardHandler(this)); // upstream 6
	}
	

	public void forward(String host, int port, PEASObject obj, Channel returnChannel, ChannelHandlerContext ctx) throws InterruptedException {
		//if (forward == null) {
			// Start the connection attempt.
	        Bootstrap b = new Bootstrap();
	        b.group(returnChannel.eventLoop())
	         .channel(ctx.channel().getClass())
	         .handler(new ForwardChannelInitializer(returnChannel, this.getAEScipher()));
	        
	        ChannelFuture f = b.connect(host, port);
	       
	        f.addListener(new ChannelFutureListener() {
	            @Override
	            public void operationComplete(ChannelFuture future) {
	                if (future.isSuccess()) {
	                	System.out.println("connected to next node");
	                } else {
	                	// TODO: normally send peas response with status code that issuer is not available
	
	                    // Close the connection if the connection attempt has failed.
	                	System.out.println("not connected to next node");
	                    returnChannel.close();
	                }
	            }
	        });
	        
	        forward = f.channel();
	        /*
		} else {
			System.out.println(forward.isWritable());
			ChannelFuture f = forward.writeAndFlush(obj);
			
			f.addListener(new ChannelFutureListener() {
		    	@Override
		        public void operationComplete(ChannelFuture future) {
		    		if (future.isSuccess()) {
		            	System.out.println("forward on existing successful");
		            } else {
		                // TODO: normally send peas response with status code that issuer is not available
		
		                // Close the connection if the connection attempt has failed.
		                System.out.println("forward on existing failed");
		                returnChannel.close();
		            }
		        }
		    });
		}
		*/

	}
	
	public void closeForward() {
		forward.close();
	}
	
	public AsymmetricBlockCipher getRSAdecipher() {
		return RSAdecipher;
	}

	public void setRSAdecipher(AsymmetricBlockCipher RSAdecipher) {
		this.RSAdecipher = RSAdecipher;
	}

	public Cipher getAEScipher() {
		return AEScipher;
	}

	public void setAEScipher(Cipher aEScipher) {
		AEScipher = aEScipher;
	}

	public Cipher getAESdecipher() {
		return AESdecipher;
	}

	public void setAESdecipher(Cipher aESdecipher) {
		AESdecipher = aESdecipher;
	}

	public void setOutboundChannel(Channel channel) {
		this.outboundChannel = channel;
	}
	
	public Channel getOutboundChannel() {
		return outboundChannel;
	}


}
