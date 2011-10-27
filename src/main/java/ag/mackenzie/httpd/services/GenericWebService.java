package ag.mackenzie.httpd.services;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import ag.mackenzie.httpd.HTTPException;
import ag.mackenzie.httpd.HTTPRequest;
import ag.mackenzie.httpd.HTTPResponse;
import ag.mackenzie.httpd.HTTPResponseCode;
import ag.mackenzie.httpd.Service;
import ag.mackenzie.httpd.ServiceException;
import ag.mackenzie.httpd.ServiceManager;

public class GenericWebService implements Service{
	public static Logger logger = Logger.getLogger("ag.mackenzie.httpd.services");
	@Override
	public void process(HTTPRequest request, HTTPResponse response)
			throws ServiceException {
		
		// handle default path.
		String path = request.getPath();
		
		if (path.startsWith("/sys/services")) {
			try {
				logger.info("Attempting to invoke service handler for: " + path);
				ServiceManager serviceManager = ServiceManager.getInstance("ag.mackenzie.httpd.services");
				Service svc = serviceManager.resolveService(path);
				svc.process(request, response);
			} catch (ServiceException e) {
				logger.severe(e.getMessage());
			}
			return;
		}
		else {
			if (path.endsWith("/")) {
				path = request.getPathTranslated() + "index.html";
			}
		}
		File requestedFile = new File(path);
		long contentLength = 0L;
		String contentType = guessMimeType(path);
		if (requestedFile.exists() && requestedFile.canRead()) {
			contentLength = requestedFile.length();
			try {
				response.writeHeaders(HTTPResponseCode.OK, contentLength, contentType, true);
			} catch (IOException e) {
				logger.severe("Writing headers: " + e.getMessage());
			}
			try {
				response.writeFile(requestedFile);
			} catch (FileNotFoundException e) {
				logger.severe("File not found: " + e.getMessage());
			} catch (IOException e) {
				logger.severe("Problem writing file content: " + e.getMessage());
			}
		}
		else {
			handleFileNotFound(path, response);
		}
	}

	private void handleFileNotFound(String path, HTTPResponse response) {
		HTTPException httpEx = new HTTPException(HTTPResponseCode.FILE_NOT_FOUND, path + " was not found on this server.");
		try {
			response.writeHeaders(httpEx.getResponseCode(), httpEx.getMessage().length(), "text/html", false);
			response.writeString(httpEx.getMessage());
		} catch (IOException e) {
			logger.severe("Error sending 404: " + e.getMessage());
		}
		
	}
	private String guessMimeType(String path) {
		String contentType = "application/octet-stream";
		if (path.endsWith(".js")) contentType = "text/javascript";
		else if (path.endsWith(".css")) contentType = "text/css";
		else if (path.endsWith(".jpg")) contentType = "image/jpeg";
		else if (path.endsWith(".gif")) contentType = "image/gif";
		else if (path.endsWith(".png")) contentType = "image/png";
		else if (path.endsWith(".html") || path.endsWith(".html")) contentType = "text/html";
		return contentType;
	}
}
