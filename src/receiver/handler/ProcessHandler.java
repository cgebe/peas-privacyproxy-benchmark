package receiver.handler;

import java.util.Random;

import io.netty.channel.ChannelHandlerContext;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import util.Message;
import io.netty.channel.SimpleChannelInboundHandler;

public class ProcessHandler extends SimpleChannelInboundHandler<Message> {
	

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	System.out.println("connection established");
    }
    
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
		// maybe unnecessary
		System.out.println("message recieved");
		System.out.println(msg.toJSONString());
		msg.setQueryId(randomString(16));
		super.channelRead(ctx, msg);
	}

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        
    }
    

    static final String ABC = "0123456789abcdefghijklmnopqrstuvwxyz";
    static Random rnd = new Random();
    
    private static String randomString(int length) {
       StringBuilder sb = new StringBuilder(length);
       for (int i = 0; i < length; i++)  {
          sb.append(ABC.charAt(rnd.nextInt(ABC.length())));
       }
       return sb.toString();
    }


}
