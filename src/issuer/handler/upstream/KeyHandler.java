package issuer.handler.upstream;

import java.nio.file.Files;
import java.nio.file.Paths;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASObject;
import protocol.PEASResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Path;

public class KeyHandler extends SimpleChannelInboundHandler<PEASObject> {

	@Override
	public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1) throws Exception {
		arg1.printStackTrace();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASObject obj) throws Exception {
		if (obj.getHeader().getCommand().equals("KEY")) {

            byte[] keyBytes = Files.readAllBytes(Paths.get(".").resolve("pubKey2.der"));
            
            // construct key response
            PEASHeader header = new PEASHeader();
            header.setCommand("RESPONSE");
            header.setIssuer(obj.getHeader().getIssuer());
            header.setStatus("100");
            header.setBodyLength(keyBytes.length);
            
            PEASBody body = new PEASBody(keyBytes.length);
            body.getBody().writeBytes(keyBytes);
            
            PEASResponse res = new PEASResponse(header, body);
            
            // send reponse back
            ChannelFuture f = ctx.writeAndFlush(res);
            
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                    	System.out.println("return key successful");
                    } else {
                        System.out.println("return key failed");
                        future.channel().close();
                    }
                }
            });

		} else {
			ctx.fireChannelRead(obj);
		}
			
	}


}