package receiver.handler.forward.upstream;

import receiver.handler.upstream.PEASPrinter;
import util.Config;
import codec.PEASDecoder3;
import codec.PEASEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ForwardChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;
	private Channel inboundChannel;
	
	public ForwardChannelInitializer(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		
		// Logging on?
		if (Config.getInstance().getValue("LOGGING").equals("on")) {
			pipeline.addLast(new LoggingHandler(LogLevel.INFO));
		}
        pipeline.addLast("peasdecoder", new PEASDecoder3());  // upstream 1
        pipeline.addLast("peasencoder", new PEASEncoder()); // downstream 1
        pipeline.addLast("peasprinter", new PEASPrinter()); // upstream 2
        pipeline.addLast("returner", new ReturnHandler(inboundChannel)); // upstream 3
	}

}
