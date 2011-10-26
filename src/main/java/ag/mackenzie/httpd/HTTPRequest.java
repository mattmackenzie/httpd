package ag.mackenzie.httpd;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class HTTPRequest {
	
	private String path;
	private HashMap<String, String> headers;
	private String webroot;

	public HTTPRequest(String path, String webroot, HashMap<String, String> headers) {
		this.path = path;
		this.headers = headers;
		this.webroot = webroot;
	}
	
	public String getHeader(String name) {
		return this.headers.get(name);
	}
	
	public HashMap<String, String> getHeaders() {
		return this.headers;
	}
	
	public String getPath() {
		if (this.path.contains("?")) {
			return this.path.split("\\?")[0];
		}
		return this.path;
	}
	
	public String getPathTranslated() {
		return this.webroot + this.path;
	}
	
	public HashMap<String, String> getURLParameters() {
		HashMap<String, String> params = new HashMap<String, String>();
		if (this.path.contains("?")) {
			String qstring = this.path.split("\\?")[1];
			for (String param : qstring.split("&")) {
	            String pair[] = param.split("=");
	            String key = "";
	            String value = "";
				try {
					key = URLDecoder.decode(pair[0], "UTF-8");
					value = URLDecoder.decode(pair[1], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
	            params.put(key, value);
	        }
		}
		return params;
	}

}
