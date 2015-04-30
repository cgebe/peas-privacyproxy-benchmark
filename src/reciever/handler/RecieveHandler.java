package reciever.handler;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import util.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RecieveHandler extends SimpleChannelInboundHandler<String> {
	
	
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
		super.channelRead(ctx, msg);
	}

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        
    }
    

	


}
