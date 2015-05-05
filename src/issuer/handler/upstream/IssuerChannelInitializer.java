package issuer.handler.upstream;

import receiver.handler.upstream.PEASPrinter;
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
		//pipeline.addLast(new LoggingHandler(LogLevel.INFO));
		pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		//pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(120));
        //pipeline.addLast("stringDecoder", new StringDecoder());
        //pipeline.addLast("stringEncoder", new StringEncoder());
		pipeline.addLast("peasdecoder", new PEASDecoder());
        pipeline.addLast("peasecoder", new PEASEncoder());
        pipeline.addLast("peasprinter", new PEASPrinter());
        pipeline.addLast("keyhandler", new KeyHandler());
        //pipeline.addLast("queryhandler", new QueryHandler());
        // TODO
        //pipeline.addLast("decrypter", new DecryptHandler());
        //pipeline.addLast("encrypter", new EncryptHandler());
        //pipeline.addLast("decider", new DecidingHandler());
        
        pipeline.addLast("dispatcher", new DispatchHandler());
	}

}
