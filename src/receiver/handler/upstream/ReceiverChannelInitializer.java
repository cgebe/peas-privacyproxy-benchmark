package receiver.handler.upstream;


import receiver.server.ReceiverServer;
import util.Config;
import codec.PEASDecoder;
import codec.PEASEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ReceiverChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;
	private ReceiverServer server;
	
	public ReceiverChannelInitializer(ReceiverServer server) {
		this.server = server;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();

		// Logging on?
		if (Config.getInstance().getValue("LOGGING").equals("on")) {
			pipeline.addLast(new LoggingHandler(LogLevel.INFO));
		}
        pipeline.addLast("peasdecoder", new PEASDecoder()); // upstream 1
        pipeline.addLast("peasencoder", new PEASEncoder()); // downstream 1
        
        if (Config.getInstance().getValue("LOGGING").equals("on")) {
        	pipeline.addLast("peasprinter", new PEASPrinter()); // upstream 2
        }
        pipeline.addLast("forwarder", new ForwardHandler(server)); // upstream 3
	}

}
