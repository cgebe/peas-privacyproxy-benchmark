package protocol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;

public class PEASMessage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8994528029805801398L;
	private PEASHeader header;
	private PEASBody body;
	
	public PEASMessage(PEASHeader header, PEASBody body) {
		this.header = header;
		this.body = body;
	}


	public String toString() {
		StringBuilder request = new StringBuilder();
		
		request.append(header.toString());
		request.append(body.getContent().toString());
		
		return request.toString();
	}
	

	public String toJSONString() {
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("command", header.getCommand());
		map.put("issuer", header.getIssuer());
		
		if (header.getProtocol() != null) {
			map.put("protocol", header.getProtocol());
		} 
		
		if (header.getQuery() != null) {
			map.put("query", header.getQuery());
		}
		
		if (body.getContent() != null && header.getContentLength() > 0) {
			map.put("body", body.getContent().toString());
			map.put("bodylength", String.valueOf(header.getContentLength()));
		}
		
		return JSONValue.toJSONString(map);
	}

	public PEASHeader getHeader() {
		return header;
	}
	
	public void setHeader(PEASHeader header) {
		this.header = header;
	}
	
	public PEASBody getBody() {
		return body;
	}
	
	public void setBody(PEASBody body) {
		this.body = body;
	}
}
