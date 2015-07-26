package util;

import java.util.List;

import protocol.PEASMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class PEASPrinterOut extends MessageToMessageEncoder<PEASMessage> {

	@Override
	protected void encode(ChannelHandlerContext arg0, PEASMessage obj, List<Object> out) throws Exception {
		System.out.println();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>> OUT >>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println(obj.toString());
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>> OUT >>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println();
		out.add(obj);
	}

}