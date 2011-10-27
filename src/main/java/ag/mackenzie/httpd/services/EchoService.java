package ag.mackenzie.httpd.services;

import java.io.IOException;

import ag.mackenzie.httpd.HTTPRequest;
import ag.mackenzie.httpd.HTTPResponse;
import ag.mackenzie.httpd.HTTPResponseCode;
import ag.mackenzie.httpd.Service;
import ag.mackenzie.httpd.ServiceException;

public class EchoService implements Service {
	public void process(HTTPRequest request, HTTPResponse response) throws ServiceException {
		String toEcho = request.getURLParameters().get("echo");
		if (toEcho == null) {
			toEcho = "null";
		}
		try {
			response.writeHeaders(HTTPResponseCode.OK, toEcho.length(), "text/plain", true);
			response.writeString(toEcho);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
		
	}

}
