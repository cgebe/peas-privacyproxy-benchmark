package onion.node.upstream;




import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

import util.Config;
import util.InputWriter;
import util.OutputWriter;
import util.PEASPrinterIn;
import util.PEASPrinterOut;
import codec.PEASDecoder;
import codec.PEASEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class NodeChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private AsymmetricKeyParameter key;
	
	public NodeChannelInitializer(AsymmetricKeyParameter key) {
		this.key = key;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		NodeChannelState channelState = new NodeChannelState(key);
		
		ChannelPipeline pipeline = ch.pipeline();
		
		// Logging on?
		if (Config.getInstance().getValue("LOGGING").equals("on")) {
			//pipeline.addLast(new LoggingHandler(LogLevel.INFO));
		}
		if (Config.getInstance().getValue("MEASURE_SERVER_STATS").equals("on")) {
			pipeline.addLast("outputwriter", new InputWriter());
			pipeline.addLast("inputwriter", new OutputWriter());
		}
        pipeline.addLast("peasdecoder", new PEASDecoder()); // upstream 1
        pipeline.addLast("peasencoder", new PEASEncoder()); // downstream 1
        
        if (Config.getInstance().getValue("LOGGING").equals("on")) {
        	pipeline.addLast("peasprinterin", new PEASPrinterIn()); // upstream 2
        	pipeline.addLast("peasprinterout", new PEASPrinterOut()); // downstream 1
        }
        pipeline.addLast("forwarder", new ForwardHandler(channelState)); // upstream 3
        pipeline.addLast("handshakehandler", new HandshakeHandler(channelState)); // upstream 4
        pipeline.addLast("queryhandler", new QueryHandler(channelState)); // upstream 5
	}



}
