package receiver.handler.forward.upstream;

import protocol.PEASObject;
import codec.JSONDecoder;
import codec.JSONEncoder;
import codec.PEASDecoder;
import codec.PEASEncoder;
import io.netty.channel.Channel;
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

public class ForwardChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;
	private Channel inboundChannel;
	private PEASObject obj;
	
	public ForwardChannelInitializer(Channel inboundChannel, PEASObject toSend) {
        this.inboundChannel = inboundChannel;
        this.obj = toSend;
    }

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		pipeline.addLast(new LoggingHandler(LogLevel.INFO));
		//pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast("framedecoder", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		//pipeline.addLast("framedecoder", new LineBasedFrameDecoder(120));
        //pipeline.addLast("stringdecoder", new StringDecoder());
        //pipeline.addLast("stringencoder", new StringEncoder());
        //pipeline.addLast("jsondecoder", new JSONDecoder());
        //pipeline.addLast("jsonencoder", new JSONEncoder());
        pipeline.addLast("peasdecoder", new PEASDecoder());
        pipeline.addLast("peasencoder", new PEASEncoder());
        pipeline.addLast("returner", new ReturnHandler(inboundChannel, obj));
	}

}
