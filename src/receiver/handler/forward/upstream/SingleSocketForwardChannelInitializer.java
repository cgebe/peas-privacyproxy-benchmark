package receiver.handler.forward.upstream;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import receiver.server.ReceiverServer;
import util.Config;
import util.InputWriter;
import util.OutputWriter;
import util.PEASPrinterIn;
import util.PEASPrinterOut;
import codec.PEASDecoder;
import codec.PEASEncoder;

public class SingleSocketForwardChannelInitializer extends ChannelInitializer<SocketChannel> {

	private ReceiverServer server;
	
	public SingleSocketForwardChannelInitializer(ReceiverServer server) {
        this.server = server;
    }

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		
		// Logging on?
		if (Config.getInstance().getValue("LOGGING").equals("on")) {
			//pipeline.addLast(new LoggingHandler(LogLevel.INFO));
		}
		if (Config.getInstance().getValue("MEASURE_SERVER_STATS").equals("on")) {
			pipeline.addLast("outputwriter", new InputWriter());
			pipeline.addLast("inputwriter", new OutputWriter());
		}
        pipeline.addLast("peasdecoder", new PEASDecoder());  // upstream 1
        pipeline.addLast("peasencoder", new PEASEncoder()); // downstream 1
        
        if (Config.getInstance().getValue("LOGGING").equals("on")) {
        	pipeline.addLast("peasprinterin", new PEASPrinterIn()); // upstream 2
        	pipeline.addLast("peasprinterout", new PEASPrinterOut()); // downstream 1
        }
        pipeline.addLast("returner", new SingleSocketReturnHandler(server)); // upstream 3
	}

}
