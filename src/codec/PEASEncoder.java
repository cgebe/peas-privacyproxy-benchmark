package codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import protocol.PEASMessage;
import util.Config;
import util.Observer;

public class PEASEncoder extends MessageToByteEncoder<PEASMessage>{
	
	 // TODO Use CharsetEncoder instead.
    private final Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public PEASEncoder() {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a new instance with the specified character set.
     */
    public PEASEncoder(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }
    
    /*
    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, PEASObject obj, boolean preferDirect) {
        return ctx.alloc().directBuffer(8192 + 8192 + 4096);
    }
     */

	@Override
	protected void encode(ChannelHandlerContext ctx, PEASMessage obj, ByteBuf out) throws Exception {
		// write header to downstream
		ByteBuf head = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(obj.getHeader().toString()), charset);
		out.writeBytes(head);
		head.release();
		// write body to downstream
		out.writeBytes(obj.getBody().getContent());
		// append line separator at the end of the body for framing
		//out.writeBytes(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(System.lineSeparator()), charset));
		if (Config.getInstance().getValue("MEASURE_PROCESS_TIME").equals("on")) {
			//System.out.println((obj.getDestructionTime() - obj.getCreationTime()) / 1e6);
			
			String jarPath = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath();
			PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter(jarPath + "/" + "process.log", true)));
			log.println((System.nanoTime() - obj.getCreationTime()) / 1e6);
			log.close();
		}
		
		if (Config.getInstance().getValue("MEASURE_SERVER_STATS") != null && Config.getInstance().getValue("MEASURE_SERVER_STATS").equals("on")) {
        	Observer.getInstance().setRequestsOut(Observer.getInstance().getRequestsOut() + 1);
        }
	}


}
