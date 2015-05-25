package codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Pattern;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASMessage;
import protocol.PEASParser;

public class PEASDecoder extends MessageToMessageDecoder<ByteBuf> {
	
	private final Charset charset;

	private boolean firstLine;
	
	private boolean writeBody;
	private int writeIndex;
	
	private PEASHeader header;
	private PEASBody body;
	
	private Pattern p;
	private ChannelHandler removed;
	
	public PEASDecoder() {
        this(Charset.defaultCharset());
    }
	
    public PEASDecoder(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
        this.writeBody = false;
        this.firstLine = true;
        this.header = new PEASHeader();
        this.p = Pattern.compile("\\s+");
        this.removed = null;
    }
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		if (!writeBody) {
			System.out.println(msg.toString(charset));
			if (firstLine) {
				String line = msg.toString(charset);
				String[] values = p.split(line);
				
				header.setCommand(values[0]);
				header.setIssuer(values[1]);
				
				firstLine = false;
			} else {
				String headerField = msg.toString(charset);
				
				// reading header is finished
				if (headerField.equals("")) {
					ctx.pipeline().remove("framedecoder");
					//removed = ctx.pipeline().replace("framedecoder", "lframedecoder", new FixedLengthFrameDecoder(1));
					writeBody = true;
					body = new PEASBody(header.getContentLength());
				}
				
				String[] values = p.split(headerField);
				
				if (values[0].equals("Status:")) {
					header.setStatus(values[1]);
				}
				
				if (values[0].equals("Query:")) {
					header.setQuery(values[1]);
				}
				
				if (values[0].equals("Protocol:")) {
					header.setProtocol(values[1]);
				}
				
				if (values[0].equals("Content-Length:")) {
					header.setContentLength(Integer.parseInt(values[1]));
				}
			}
		} else {
			System.out.println("write body");
			if (header.getContentLength() <= 0) {
				out.add(new PEASMessage(header, body));

				// add decoder again
				//ctx.pipeline().replace("lframedecoder", "framedecoder", removed);
				writeIndex = 0;
				writeBody = false;
				this.header = new PEASHeader();
			} else {
				writeIndex += msg.capacity();
				body.getContent().writeBytes(msg);
				System.out.println(msg.capacity());
				System.out.println(writeIndex);
				if (writeIndex + 1 >= header.getContentLength()) {
					out.add(new PEASMessage(header, body));

					// add decoder again
					//ctx.pipeline().replace("lframedecoder", "framedecoder", removed);
					writeIndex = 0;
					writeBody = false;
					this.header = new PEASHeader();
				}
			}
		}
		//out.add(PEASParser.parse(json));
	}
}
