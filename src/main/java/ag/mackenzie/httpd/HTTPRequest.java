package ag.mackenzie.httpd;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class HTTPRequest {
	
	private String path;
	private HashMap<String, String> headers;

	public HTTPRequest(String path, HashMap<String, String> headers) {
		this.path = path;
		this.headers = headers;
	}
	
	public String getHeader(String name) {
		return this.headers.get(name);
	}
	
	public String getPath() {
		if (this.path.contains("?")) {
			return this.path.split("\\?")[0];
		}
		return this.path;
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
