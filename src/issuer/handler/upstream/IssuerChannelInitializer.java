package issuer.handler.upstream;

import receiver.handler.upstream.PEASPrinter;
import util.Config;
import codec.PEASDecoder;
import codec.PEASDecoder;
import codec.PEASEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class IssuerChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;

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
        	pipeline.addLast("peasprinter", new PEASPrinter());
        }
        pipeline.addLast("keyhandler", new KeyHandler()); // upstream 3
        pipeline.addLast("queryhandler", new QueryHandler()); // upstream 4
        
        // TODO
        //pipeline.addLast("dispatcher", new DispatchHandler());
	}

}
