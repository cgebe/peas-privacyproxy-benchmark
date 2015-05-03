package receiver.handler.out;

import codec.JSONDecoder;
import codec.JSONEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SendChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;
	private Channel inboundChannel;
	
	public SendChannelInitializer(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
		//pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast("framedecoder", new LineBasedFrameDecoder(120));
        pipeline.addLast("stringdecoder", new StringDecoder());
        pipeline.addLast("stringencoder", new StringEncoder());
        pipeline.addLast("jsondecoder", new JSONDecoder());
        pipeline.addLast("jsonencoder", new JSONEncoder());
        pipeline.addLast("returner", new ReturnHandler(inboundChannel));
	}

}
