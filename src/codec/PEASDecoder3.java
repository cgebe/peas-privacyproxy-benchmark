package codec;

import java.nio.charset.Charset;
import java.util.List;

import protocol.PEASBody;
import protocol.PEASHeader;
import protocol.PEASMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.util.internal.AppendableCharSequence;


public class PEASDecoder3 extends ByteToMessageDecoder {
	private static final String EMPTY_VALUE = "";
	
	private PEASHeader header;
	private PEASBody body;
	
	private State currentState = State.READ_INITIAL;

	private int maxContentSize;
	private long contentLength = Long.MIN_VALUE;
	
    private CharSequence name;
    private CharSequence value;
    
	private LineParser lineParser;
	private HeaderParser headerParser;

	private long chunkSize;
	
	private enum State {
		READ_INITIAL,
		READ_HEADER,
		READ_CONTENT,
		BAD_MESSAGE;
	}

	
    public PEASDecoder3() {
        this(4096, 8192, 8192, Charset.defaultCharset());
    }

    /**
     * Creates a new instance with the specified parameters.
     */
    public PEASDecoder3(int maxInitialLineLength, int maxHeaderSize, int maxContentSize, Charset charset) {
    	if (charset == null) {
            throw new NullPointerException("charset");
        }
    	if (maxInitialLineLength <= 0) {
            throw new IllegalArgumentException(
                    "maxInitialLineLength must be a positive integer: " +
                     maxInitialLineLength);
        }
        if (maxHeaderSize <= 0) {
            throw new IllegalArgumentException(
                    "maxHeaderSize must be a positive integer: " +
                    maxHeaderSize);
        }
        if (maxContentSize <= 0) {
            throw new IllegalArgumentException(
                    "maxContentize must be a positive integer: " +
                    maxContentSize);
        }
        this.maxContentSize = maxContentSize;
        AppendableCharSequence seq = new AppendableCharSequence(128);
        lineParser = new LineParser(seq, maxInitialLineLength);
        headerParser = new HeaderParser(seq, maxHeaderSize);
    }

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
		switch (currentState) {
		case READ_INITIAL: try {
	        AppendableCharSequence line = lineParser.parse(buffer);
	        if (line == null) {
	            return;
	        }
	        String[] initialLine = splitInitialLine(line);
	        if (initialLine.length < 3) {
	            // Invalid initial line - ignore.
	            currentState = State.BAD_MESSAGE;
	            return;
	        }
	
	        this.header = createHeader(initialLine);
	        currentState = State.READ_HEADER;
	        // fall-through
	    } catch (Exception e) {
	        out.add(invalidMessage(buffer, e));
	        return;
	    }
	    case READ_HEADER: 
	    try {
	        State nextState = readHeaders(buffer);
	        if (nextState == null) {
	            return;
	        }
	        currentState = nextState;

            long contentLength = contentLength();
            if (contentLength == 0) {
                out.add(new PEASMessage(this.header, new PEASBody(0)));
                resetNow();
                return;
            }

            assert nextState == State.READ_CONTENT;

            if (nextState == State.READ_CONTENT) {
                // chunkSize will be decreased as the READ_CONTENT state reads data chunk by chunk.
                chunkSize = contentLength;
            }
            
            body = new PEASBody(header.getContentLength());
            // We return here, this forces decode to be called again where we will decode the content
            return;
	        
	    } catch (Exception e) {
	        out.add(invalidMessage(buffer, e));
	        return;
	    }
	    case READ_CONTENT: {
	        int readLimit = buffer.readableBytes();

	        // Check if the buffer is readable first as we use the readable byte count
	        // to write the PEASBody. This is needed as otherwise we may end up with
	        // create a Body instance that contains an empty buffer and so is
	        // handled like it is the last one.

	        if (readLimit == 0) {
	        	System.out.println("ret 0");
	            return;
	        }
	
	        int toRead = Math.min(readLimit, maxContentSize);
	        if (toRead > chunkSize) {
	            toRead = (int) chunkSize;
	        }
	        ByteBuf content = buffer.readSlice(toRead).retain();
	        chunkSize -= toRead;
	        if (chunkSize == 0) {
		        System.err.println("Decoder: whole msg received");
	            // Read all content.
	        	body.getContent().writeBytes(content);
	            out.add(new PEASMessage(this.header, body));
	            resetNow();
	        } else {
	        	System.err.println("Decoder: partially msg received");
	        	body.getContent().writeBytes(content);
	        }
	        return;
	    }
		default:
			break;
	    }
	}

    private void resetNow() {
    	this.header = null;
    	this.body = null;
    	name = null;
        value = null;
        contentLength = Long.MIN_VALUE;
        lineParser.reset();
        headerParser.reset();
        currentState = State.READ_INITIAL;
    }

    private PEASHeader invalidMessage(ByteBuf in, Exception cause) {
        currentState = State.BAD_MESSAGE;

        // Advance the readerIndex so that ByteToMessageDecoder does not complain
        // when we produced an invalid message without consuming anything.
        in.skipBytes(in.readableBytes());

        return new PEASHeader();
    }

    private State readHeaders(ByteBuf buffer) {
        AppendableCharSequence line = headerParser.parse(buffer);
        if (line == null) {
            return null;
        }
        if (line.length() > 0) {
            do {
                char firstChar = line.charAt(0);
                if (name != null && (firstChar == ' ' || firstChar == '\t')) {
                    StringBuilder buf = new StringBuilder(value.length() + line.length() + 1);
                    buf.append(value)
                       .append(' ')
                       .append(line.toString().trim());
                    value = buf.toString();
                } else {
                    if (name != null) {
                    	if (name.toString().equals("Status")) {
                    		this.header.setStatus(value.toString());
                    	}
                    	if (name.toString().equals("Forward")) {
                    		this.header.setForward(value.toString());
                    	}
                    	if (name.toString().equals("Protocol")) {
                    		this.header.setProtocol(value.toString());
                    	}
                    	if (name.toString().equals("Content-Length")) {
                    		this.header.setContentLength(Integer.parseInt(value.toString()));
                    	}
                    	if (name.toString().equals("Query")) {
                    		this.header.setQuery(value.toString());
                    	}
                    }
                    splitHeader(line);
                }

                line = headerParser.parse(buffer);
                if (line == null) {
                    return null;
                }
            } while (line.length() > 0);
        }

        // Add the last header.
        if (name != null) {
        	if (name.toString().equals("Status")) {
        		this.header.setStatus(value.toString());
        	}
        	if (name.toString().equals("Forward")) {
        		this.header.setForward(value.toString());
        	}
        	if (name.toString().equals("Protocol")) {
        		this.header.setProtocol(value.toString());
        	}
        	if (name.toString().equals("Content-Length")) {
        		this.header.setContentLength(Integer.parseInt(value.toString()));
        	}
        	if (name.toString().equals("Query")) {
        		this.header.setQuery(value.toString());
        	}
        }
        // reset name and value fields
        name = null;
        value = null;

        return State.READ_CONTENT;
    }

    private long contentLength() {
        if (contentLength == Long.MIN_VALUE) {
            contentLength = this.header.getContentLength();
        }
        return contentLength;
    }

    private static String[] splitInitialLine(AppendableCharSequence sb) {
        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;

        aStart = findNonWhitespace(sb, 0);
        aEnd = findWhitespace(sb, aStart);

        bStart = findNonWhitespace(sb, aEnd);
        bEnd = findWhitespace(sb, bStart);

        cStart = findNonWhitespace(sb, bEnd);
        cEnd = findEndOfString(sb);

        return new String[] {
                sb.substring(aStart, aEnd),
                sb.substring(bStart, bEnd),
                cStart < cEnd? sb.substring(cStart, cEnd) : "" };
    }

    private void splitHeader(AppendableCharSequence sb) {
        final int length = sb.length();
        int nameStart;
        int nameEnd;
        int colonEnd;
        int valueStart;
        int valueEnd;

        nameStart = findNonWhitespace(sb, 0);
        for (nameEnd = nameStart; nameEnd < length; nameEnd ++) {
            char ch = sb.charAt(nameEnd);
            if (ch == ':' || Character.isWhitespace(ch)) {
                break;
            }
        }

        for (colonEnd = nameEnd; colonEnd < length; colonEnd ++) {
            if (sb.charAt(colonEnd) == ':') {
                colonEnd ++;
                break;
            }
        }

        name = sb.substring(nameStart, nameEnd);
        valueStart = findNonWhitespace(sb, colonEnd);
        if (valueStart == length) {
            value = EMPTY_VALUE;
        } else {
            valueEnd = findEndOfString(sb);
            value = sb.substring(valueStart, valueEnd);
        }
    }

    private static int findNonWhitespace(CharSequence sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (!Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private static int findWhitespace(CharSequence sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private static int findEndOfString(CharSequence sb) {
        int result;
        for (result = sb.length(); result > 0; result --) {
            if (!Character.isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }
	
	private static class HeaderParser implements ByteBufProcessor {
        private final AppendableCharSequence seq;
        private final int maxLength;
        private int size;

        HeaderParser(AppendableCharSequence seq, int maxLength) {
            this.seq = seq;
            this.maxLength = maxLength;
        }

        public AppendableCharSequence parse(ByteBuf buffer) {
            final int oldSize = size;
            seq.reset();
            int i = buffer.forEachByte(this);
            if (i == -1) {
                size = oldSize;
                return null;
            }
            buffer.readerIndex(i + 1);
            return seq;
        }

        public void reset() {
            size = 0;
        }

        @Override
        public boolean process(byte value) throws Exception {
            char nextByte = (char) value;
            if (nextByte == HttpConstants.CR) {
                return true;
            }
            if (nextByte == HttpConstants.LF) {
                return false;
            }

            if (++ size > maxLength) {
                // TODO: Respond with Bad Request and discard the traffic
                //    or close the connection.
                //       No need to notify the upstream handlers - just log.
                //       If decoding a response, just throw an exception.
                throw newException(maxLength);
            }

            seq.append(nextByte);
            return true;
        }

        protected TooLongFrameException newException(int maxLength) {
            return new TooLongFrameException("PEAS header is larger than " + maxLength + " bytes.");
        }
    }

	
	private static final class LineParser extends HeaderParser {

        LineParser(AppendableCharSequence seq, int maxLength) {
            super(seq, maxLength);
        }

        @Override
        public AppendableCharSequence parse(ByteBuf buffer) {
            reset();
            return super.parse(buffer);
        }

        @Override
        protected TooLongFrameException newException(int maxLength) {
            return new TooLongFrameException("An PEAS line is larger than " + maxLength + " bytes.");
        }
    }
	
	private PEASHeader createHeader(String[] initialLine) {
		PEASHeader header = new PEASHeader();
		header.setCommand(initialLine[0]);
		header.setIssuer(initialLine[1]);
		return header;
	}
}
