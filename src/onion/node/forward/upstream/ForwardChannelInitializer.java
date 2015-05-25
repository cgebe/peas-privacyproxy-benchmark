package onion.node.forward.upstream;

import javax.crypto.Cipher;

import protocol.PEASMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import receiver.handler.upstream.PEASPrinter;
import util.Config;
import codec.JSONDecoder;
import codec.JSONEncoder;
import codec.PEASDecoder;
import codec.PEASEncoder;

public class ForwardChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;
	private Channel inboundChannel;
	private PEASMessage obj;
	private Cipher cipher;
	
	public ForwardChannelInitializer(Channel inboundChannel, Cipher AEScipher) {
        this.inboundChannel = inboundChannel;
        //this.obj = toSend;
        this.cipher = AEScipher;
    }

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		
		// Logging on?
		if (Config.getInstance().getValue("LOGGING").equals("on")) {
			pipeline.addLast(new LoggingHandler(LogLevel.INFO));
		}
        pipeline.addLast("peasdecoder", new PEASDecoder());  // upstream 1
        pipeline.addLast("peasencoder", new PEASEncoder()); // downstream 1
        
        if (Config.getInstance().getValue("LOGGING").equals("on")) {
        	pipeline.addLast("peasprinter", new PEASPrinter()); // upstream 2
        }
        pipeline.addLast("returner", new ReturnHandler(inboundChannel, cipher)); // upstream 3
	}

}

