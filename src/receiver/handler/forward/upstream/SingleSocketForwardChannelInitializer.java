package receiver.handler.forward.upstream;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import receiver.handler.upstream.PEASPrinter;
import receiver.server.ReceiverServer;
import util.Config;
import codec.PEASDecoder3;
import codec.PEASEncoder;

public class SingleSocketForwardChannelInitializer extends ChannelInitializer<SocketChannel> {
	private ChannelPipeline pipeline;
	private ReceiverServer server;
	
	public SingleSocketForwardChannelInitializer(ReceiverServer server) {
        this.server = server;
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
        pipeline.addLast("returner", new SingleSocketReturnHandler(server)); // upstream 3
	}

}
