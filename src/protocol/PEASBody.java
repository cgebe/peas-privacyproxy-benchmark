package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PEASBody {
	
	private ByteBuf body;
	
	public PEASBody(int length) {
		body = Unpooled.buffer(length);
	}
	
	public PEASBody(ByteBuf body) {
		this.body = body;
	}
	
	public PEASBody(byte[] body) {
		this.body = Unpooled.wrappedBuffer(body);
	}

	public ByteBuf getBody() {
		return body;
	}

	public void setBody(ByteBuf body) {
		this.body = body;
	}

}
