package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Pattern;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASObject;
import protocol.PEASParser;
import protocol.PEASRequest;
import protocol.PEASResponse;

public class PEASDecoder extends MessageToMessageDecoder<ByteBuf> {
	
	private final Charset charset;

	private boolean firstLine;
	
	private boolean writeBody;
	private int writeIndex;
	
	private PEASHeader header;
	private PEASBody body;
	
	private Pattern p;
	
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
					System.out.println("finished");
					writeBody = true;
					body = new PEASBody(header.getBodyLength());
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
					header.setBodyLength(Integer.parseInt(values[1]));
				}
			}
		} else {
			System.out.println("writing body");
			if (writeIndex >= header.getBodyLength()) {
				if (header.getCommand().equals("KEY") || header.getCommand().equals("QUERY")) {
					out.add(new PEASRequest(header, body));
				} else {
					out.add(new PEASResponse(header, body));
				}
				writeIndex = 0;
				writeBody = false;
				this.header = new PEASHeader();
			} else {
				System.out.println(writeIndex);
				writeIndex += msg.capacity();
				System.out.println(body.getBody().writerIndex());
				body.getBody().writeBytes(msg);
			}
		}
		//out.add(PEASParser.parse(json));
	}
}