package reciever.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class RecieverChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
		//pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(120));
        pipeline.addLast("stringDecoder", new StringDecoder());
        pipeline.addLast("stringEncoder", new StringEncoder());
        pipeline.addLast("reciever", new RecieveHandler());
        pipeline.addLast("processor", new ProcessHandler());
        pipeline.addLast("forwarder", new ForwardHandler("hostfrommessage", 80));
	}

}
