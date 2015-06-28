package issuer.handler.upstream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASMessage;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


import org.apache.commons.io.IOUtils;

public class KeyHandler extends SimpleChannelInboundHandler<PEASMessage> {

	@Override
	public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1) throws Exception {
		arg1.printStackTrace();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASMessage obj) throws Exception {
		if (obj.getHeader().getCommand().equals("KEY")) {

            //byte[] keyBytes = Files.readAllBytes(Paths.get("./resources/").resolve("pubKey2.der"));
			String jarPath = new File(KeyHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath();
			InputStream inputStream = new FileInputStream(new File(jarPath + "/resources/pubKey2.der"));
            //InputStream inputStream = KeyHandler.class.getClassLoader().getResourceAsStream("pubKey2.der");
            byte[] keyBytes = IOUtils.toByteArray(inputStream);
            
            // construct key response
            PEASHeader header = new PEASHeader();
            header.setCommand("RESPONSE");
            header.setIssuer(obj.getHeader().getIssuer());
            header.setStatus("100");
            header.setContentLength(keyBytes.length);
            
            PEASBody body = new PEASBody(keyBytes.length);
            body.getContent().writeBytes(keyBytes);
            
            PEASMessage res = new PEASMessage(header, body);
            
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
