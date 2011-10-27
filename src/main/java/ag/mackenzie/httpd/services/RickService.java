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
		String video = "<object classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" width=\"437\" height=\"378\" id=\"viddler_7faf8e53\"><param name=\"flashvars\" value=\"autoplay=t\" /><param name=\"movie\" value=\"http://www.viddler.com/simple/7faf8e53/\" /><param name=\"allowScriptAccess\" value=\"always\" /><param name=\"allowFullScreen\" value=\"true\" /><embed src=\"http://www.viddler.com/simple/7faf8e53/\" width=\"437\" height=\"378\" type=\"application/x-shockwave-flash\" allowScriptAccess=\"always\" flashvars=\"autoplay=t\" allowFullScreen=\"true\" name=\"viddler_7faf8e53\"></embed></object>";
		String page = "<html><head><title>Got ya!</title></head><body><blink><h1>GOT YA!!!!</h1>" + video + "</body></html>";
		try {
			response.writeHeaders(HTTPResponseCode.OK, page.length(), "text/html", false);
			response.writeString(page);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	
}
