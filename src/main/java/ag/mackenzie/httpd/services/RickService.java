package ag.mackenzie.httpd.services;

import java.io.IOException;

import ag.mackenzie.httpd.HTTPRequest;
import ag.mackenzie.httpd.HTTPResponse;
import ag.mackenzie.httpd.HTTPResponseCode;
import ag.mackenzie.httpd.Service;
import ag.mackenzie.httpd.ServiceException;

public class RickService implements Service {

	@Override
	public void process(HTTPRequest request, HTTPResponse response)
			throws ServiceException {
		String video = "<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/dQw4w9WgXcQ\" frameborder=\"0\" allowfullscreen></iframe>";
		String page = "<html><head><title>Got ya!</title></head><body><blink><h1>GOT YA!!!!</h1>" + video + "</body></html>";
		try {
			response.writeHeaders(HTTPResponseCode.OK, page.length(), "text/html", false);
			response.writeString(page);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	
}
