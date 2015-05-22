package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PEASBody {
	
	private ByteBuf content;
	
	public PEASBody(int length) {
		content = Unpooled.buffer(length);
	}
	
	public PEASBody(ByteBuf body) {
		this.content = body;
	}
	
	public PEASBody(byte[] body) {
		this.content = Unpooled.wrappedBuffer(body);
		//this.body.writeBytes(body);
	}

	public ByteBuf getContent() {
		return content;
	}

	public void setContent(ByteBuf content) {
		this.content = content;
	}

}
