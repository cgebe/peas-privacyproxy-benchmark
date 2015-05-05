package server.dao;

public class Query {
	
	private String query;
	private String protocol;
	private String request;
	private String key;

	public Query(String query, String key, String protocol, String request) {
		this.setQuery(query);
		this.setKey(key);
		this.setProtocol(protocol);
		this.setRequest(request);
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
