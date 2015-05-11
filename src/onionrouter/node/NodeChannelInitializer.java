package onionrouter.node;

import receiver.handler.upstream.PEASPrinter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import codec.PEASDecoder;
import codec.PEASDecoder2;
import codec.PEASDecoder3;
import codec.PEASEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.FixedLengthFrameDecoder;

public class NodeChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		//pipeline.addLast("framer", new FixedLengthFrameDecoder(1));
        pipeline.addLast("peasdecoder", new PEASDecoder3()); // upstream 1
        pipeline.addLast("peasencoder", new PEASEncoder()); // downstream 1
        pipeline.addLast("peasprinter", new PEASPrinter()); // upstream 2
        pipeline.addLast("handshake", new HandshakeHandler()); // upstream 3
        pipeline.addLast("forwarder", new HandshakeForwardHandler()); // upstream 4
        pipeline.addLast("forwarder", new ForwardHandler()); // upstream 5
	}

}
