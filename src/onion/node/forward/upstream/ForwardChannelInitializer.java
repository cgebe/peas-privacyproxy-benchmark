package onion.node.forward.upstream;

import javax.crypto.Cipher;

import protocol.PEASObject;
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
import codec.JSONDecoder;
import codec.JSONEncoder;
import codec.PEASDecoder3;
import codec.PEASEncoder;

public class ForwardChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;
	private Channel inboundChannel;
	private PEASObject obj;
	private Cipher cipher;
	
	public ForwardChannelInitializer(Channel inboundChannel, Cipher AEScipher) {
        this.inboundChannel = inboundChannel;
        //this.obj = toSend;
        this.cipher = AEScipher;
    }

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast("peasdecoder", new PEASDecoder3());  // upstream 1
        pipeline.addLast("peasencoder", new PEASEncoder()); // downstream 1
        pipeline.addLast("peasprinter", new PEASPrinter()); // upstream 2
        pipeline.addLast("returner", new ReturnHandler(inboundChannel, cipher)); // upstream 3
	}

}

