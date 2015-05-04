package receiver.handler.upstream;

import codec.JSONDecoder;
import codec.JSONEncoder;
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

public class ReceiverChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		//pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
		//pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast("framedecoder", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		//pipeline.addLast("framedecoder", new LineBasedFrameDecoder(80));
        //pipeline.addLast("stringdecoder", new StringDecoder());
        //pipeline.addLast("stringencoder", new StringEncoder());
        //pipeline.addLast("jsondecoder", new JSONDecoder());
        //pipeline.addLast("jsonencoder", new JSONEncoder());
        pipeline.addLast("peasdecoder", new PEASDecoder());
        pipeline.addLast("peasencoder", new PEASEncoder());
        pipeline.addLast("sprocessor", new PEASPrinter());
        //pipeline.addLast("sprocessor", new StringPrinter());
        //pipeline.addLast("processor", new ProcessHandler());
        //pipeline.addLast("forwarder", new ForwardHandler());
	}

}
