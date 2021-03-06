package onion.node.forward.upstream;


import onion.node.upstream.NodeChannelState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import util.Config;
import util.InputWriter;
import util.OutputWriter;
import util.PEASPrinterIn;
import util.PEASPrinterOut;
import codec.PEASDecoder;
import codec.PEASEncoder;

public class ForwardChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private ChannelPipeline pipeline;
	private Channel inboundChannel;
	private NodeChannelState channelState;
	
	public ForwardChannelInitializer(Channel inboundChannel, NodeChannelState channelState) {
        this.inboundChannel = inboundChannel;
        //this.obj = toSend;
        this.channelState = channelState;
    }

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		
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
        pipeline.addLast("returner", new ReturnHandler(inboundChannel, channelState)); // upstream 3
	}

}

