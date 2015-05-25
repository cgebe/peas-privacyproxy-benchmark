package onion.node.forward.upstream;

import javax.crypto.Cipher;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import protocol.PEASBody;
import protocol.PEASMessage;


public class ReturnHandler extends SimpleChannelInboundHandler<PEASMessage> {

	private final Channel inboundChannel;
	private Cipher cipher;

    public ReturnHandler(Channel inboundChannel, Cipher AEScipher) {
        this.inboundChannel = inboundChannel;
        this.cipher = AEScipher;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, PEASMessage toReturn) throws Exception {
		toReturn.getHeader().setIssuer("ONION");
		// encrypt the return msg
		byte[] enc = cipher.doFinal(toReturn.getBody().getContent().array());
		// set new bodylength
		toReturn.getHeader().setContentLength(enc.length);
		PEASBody body = new PEASBody(enc);
		toReturn.setBody(body);
		
        ChannelFuture f = inboundChannel.writeAndFlush(toReturn);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                	System.out.println("successful return");
                } else {
                	System.out.println("failed return");
                    future.channel().close();
                }
            }
        });
	}


}