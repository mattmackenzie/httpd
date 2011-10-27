package ag.mackenzie.httpd.services;

import java.io.IOException;
import java.util.Iterator;

import ag.mackenzie.httpd.HTTPRequest;
import ag.mackenzie.httpd.HTTPResponse;
import ag.mackenzie.httpd.HTTPResponseCode;
import ag.mackenzie.httpd.Service;
import ag.mackenzie.httpd.ServiceException;

public class HeaderEchoService implements Service {

	
	public void process(HTTPRequest request, HTTPResponse response)
			throws ServiceException {
		
		Iterator<String> iter = request.getHeaders().keySet().iterator();
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><ul>");
		while (iter.hasNext()) {
			String key = iter.next();
			String value = request.getHeader(key);
			sb.append("<li>").append(key).append("<i>&nbsp;:&nbsp;</i>").append(value).append("</li>");
		}
		sb.append("</ul></body></html>");
		
		String toWrite = sb.toString();
		
		try {
			response.writeHeaders(HTTPResponseCode.OK, toWrite.length(), "text/html", false);
			response.writeString(toWrite);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

}
