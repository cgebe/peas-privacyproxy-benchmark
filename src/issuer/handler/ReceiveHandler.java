package issuer.handler;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import util.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class ReceiveHandler extends SimpleChannelInboundHandler<String> {
	
	
	private JSONParser parser = new JSONParser();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //ctx.writeAndFlush(nextQuote());
    }
    
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String str) throws Exception {
		JSONObject obj = (JSONObject) parser.parse(str);
		Message msg = new Message();
		msg.setCommand((String) obj.get("command"));
		msg.setQueryId((String) obj.get("id"));
		msg.setQuery((String) obj.get("query"));
		msg.setProtocol((String) obj.get("protocol"));
		msg.setRequest((String) obj.get("request"));
		msg.setSymmetricKey((String) obj.get("skey"));
		msg.setAsymmetricKey((String) obj.get("akey"));
		
		//String jsonPayload = "...";
		//ByteBuf buffer = Unpooled.copiedBuffer(jsonPayload, CharsetUtil.UTF_8);
		//ctx.writeAndFlush(buffer);
	}

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        
    }
    

	


}