package codec;

import java.util.List;

import protocol.PEASMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class JSONEncoder extends MessageToMessageEncoder<PEASMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, PEASMessage obj, List<Object> out) throws Exception {
		out.add(obj.toJSONString());
	}

}
