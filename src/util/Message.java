package util;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;

public class Message {
	
	private String id;
	private String command;
	
	private String query;
	private String request;
	private String protocol;
	
	private String akey;
	private String skey;
	

	public Message() {
		this.command = null;
		this.id = null;
		this.query = null;
		this.protocol = null;
		this.request = null;
		this.skey = null;
		this.akey = null;
	}
	
	// what to do? getting asymmetric key, doing query etc.
	public String getCommand() {
		return this.command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}

	// set by the proxy to determine the identity of the searcher
	public String getQueryId() {
		return this.id;
	}
	
	public void setQueryId(String id) {
		this.id = id;
	}
	
	// query of the searcher, modular for the issuer to build group profile
	public String getQuery() {
		return this.query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	// request protocol, in which protocol issuer has to send the request, mostly http
	public String getProtocol() {
		return this.protocol;
	}
	
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	public String getRequest() {
		return this.request;
	}
	
	public void setRequest(String request) {
		this.request = request;
	}

	// for key exchange
	public String getSymmetricKey() {
		return this.skey;
	}
	
	public void setSymmetricKey(String skey) {
		this.skey = skey;
	}
	
	public String getAsymmetricKey() {
		return this.akey;
	}
	
	public void setAsymmetricKey(String akey) {
		this.akey = akey;
	}
	
	public String toJSONString() {
		Map<String, String> msg = new HashMap<String, String>();
		/*
		msg.put("command", this.command);
		msg.put("id", this.id);
		msg.put("query", this.query);
		msg.put("protocol", this.protocol);
		msg.put("request", this.request);
		msg.put("skey", this.skey);
		msg.put("akey", this.akey);
		*/
		if (this.command != null) {
			msg.put("command", this.command);
		}
		if (this.id != null) {
			msg.put("id", this.id);
		}
		
		if (this.query != null) {
			msg.put("query", this.query);
		}
		
		if (this.protocol != null) {
			msg.put("protocol", this.protocol);
		}
		
		if (this.request != null) {
			msg.put("request", this.request);
		}
		
		if (this.skey != null) {
			msg.put("skey", this.skey);
		}
		
		if (this.akey != null) {
			msg.put("akey", this.akey);
		}
		
		return JSONValue.toJSONString(msg);
	}
}
