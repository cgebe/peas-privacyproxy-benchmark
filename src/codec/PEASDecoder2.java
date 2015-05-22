package codec;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASRequest;
import protocol.PEASResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class PEASDecoder2 extends DelimiterBasedFrameDecoder {

	private final Charset charset;

	private boolean firstLine;
	
	private boolean writeBody;
	private int writeIndex;
	
	private PEASHeader header;
	private PEASBody body;
	
	private Pattern p;
	private ChannelHandler removed;

	public PEASDecoder2(int maxFrameLength, ByteBuf[] delimiter) {
		super(maxFrameLength, delimiter);
        this.charset = Charset.defaultCharset();
        this.writeBody = false;
        this.firstLine = true;
        this.header = new PEASHeader();
        this.p = Pattern.compile("\\s+");
        this.removed = null;
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
		if (!writeBody) {
			Object msg = super.decode(ctx,  buffer);
			if (msg != null) {
				ByteBuf field = (ByteBuf) msg;
				System.out.println(field.toString(charset));
				if (firstLine) {
					String line = field.toString(charset);
					String[] values = p.split(line);
					
					header.setCommand(values[0]);
					header.setIssuer(values[1]);
					
					firstLine = false;
				} else {
					String headerField = field.toString(charset);
					
					// reading header is finished
					if (headerField.equals("")) {
						//ctx.pipeline().remove("framedecoder");
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
			}
			return null;
		} else {
			System.out.println("write body");
			if (header.getContentLength() <= 0) {
				if (header.getCommand().equals("KEY") || header.getCommand().equals("QUERY")) {
					return new PEASRequest(header, body);
				} else {
					return new PEASResponse(header, body);
				}
				// add decoder again
				//ctx.pipeline().replace("lframedecoder", "framedecoder", removed);
			} else {
				writeIndex += buffer.capacity();
				body.getContent().writeBytes(buffer);
				System.out.println(buffer.capacity());
				System.out.println(writeIndex);
				if (writeIndex + 1 == header.getContentLength()) {
					if (header.getCommand().equals("QUERY")) {
						return new PEASRequest(header, body);
					} else {
						return new PEASResponse(header, body);
					}
					// add decoder again
					//ctx.pipeline().replace("lframedecoder", "framedecoder", removed);
				} else {
					return null;
				}
			}
		}
	}

}
