package ag.mackenzie.httpd;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class HttpConnectionHandler extends Thread {

	private Socket socket;
	private String webRoot;
	private Logger logger;
	private BufferedReader input;
	private DataOutputStream output;
	private Timer socketKiller;
	
	public HttpConnectionHandler(Socket socket, String webRoot, Logger logger) {
		this.socket = socket;
		this.webRoot = webRoot;
		this.logger = logger;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		socketKiller = new Timer();
		socketKiller.schedule(new SocketCloseTask(), 20*1000);
		try {
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException exception) {
			logger.throwing(HttpConnectionHandler.class.getName(), "run", exception);
			return;
		}
		
		try {
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException exception) {
			logger.throwing(HttpConnectionHandler.class.getName(), "run", exception);
			return;
		}
		while (!socket.isClosed()) {
			handleRequest();
		}
		
	}
	class SocketCloseTask extends TimerTask {
		public void run() {
			try {
				socket.close();
				socketKiller.cancel();
			} catch (IOException exception) {
				logger.info("Problem closing socket: " + exception.getMessage());
			}
		}
	}
	private void handleRequest() {
		LinkedList<String> headers = new LinkedList<String>();
		HashMap<String,String> headerProperties = new HashMap<String, String>();
		String line;
		String method;
		String path;
		
		try {
			while ((line = input.readLine()) != null) {
				if (line.length() > 0) {
					headers.add(line);
					logger.fine("r:(" + line.length() + ") " + line);
				}
				else {
					break;
				}
			}
		} catch (IOException exception) {
			// almost always seems to be normal socket closure.
		}
		
		if (headers != null && headers.size() > 0) {
			String[] directive = headers.get(0).split(" ");
			method = directive[0];
			path = directive[1];
			for (int i = 1; i < headers.size(); i++) {
				String[] h = headers.get(i).split(" ");
				String name = h[0].substring(0, h[0].length()-1);
				String value = h[1];
				headerProperties.put(name, value);
			}
		}
		else {
			logger.fine("Strange, the headers are null or 0 length. Returning.");
			return;
		}
		
		HTTPRequest request = new HTTPRequest(path, headerProperties);
		HTTPResponse response = new HTTPResponse(output);
		if (method.equalsIgnoreCase("GET")) {		
			handleHttpGet(request, response);
		}
		else {
			handleUnsupported(method, response);
		}
	}

	private void handleUnsupported(String method, HTTPResponse response) {
		HTTPException httpEx = new HTTPException(HTTPResponseCode.UNIMPLEMENTED, method + " is not implemented.");
		try {
			response.writeHeaders(httpEx.getResponseCode(), httpEx.toString().length(), "text/html");
			response.writeString(httpEx.getMessage());
			socketKiller.cancel();
		} catch (IOException exception) {
			logger.info("Failed to send 501. " + exception.getMessage());
		}
	}
	
	private void handleFileNotFound(String path, HTTPResponse response) {
		HTTPException httpEx = new HTTPException(HTTPResponseCode.FILE_NOT_FOUND, path + " was not found on this server.");
		
		try {
			response.writeHeaders(httpEx.getResponseCode(), httpEx.toString().length(), "text/html");
			response.writeString(httpEx.getMessage());
			socketKiller.cancel();
		} catch (IOException e) {
			logger.severe("Error sending 404: " + e.getMessage());
		}
		
	}
	private void handleHttpGet(HTTPRequest request, HTTPResponse response) {
		
		// handle default path.
		String path = request.getPath();
		if (path.endsWith("/")) {
			path = path + "index.html";
		}
		
		File requestedFile = new File(webRoot + path);
		long contentLength = 0L;
		String contentType = guessMimeType(path);
		
		if (path.startsWith("/services/")) {
			try {
				logger.info("Attempting to invoke service handler for: " + path);
				Service svc = ServiceManager.resolveService(path);
				svc.process(request, response);
			} catch (ServiceException e) {
				logger.severe(e.getMessage());
			}
			return;
		}
		
		if (requestedFile.exists() && requestedFile.canRead()) {
			logger.info("Thread " + getId() + " Serving " + path);
			contentLength = requestedFile.length();
			try {
				response.writeHeaders(HTTPResponseCode.OK, contentLength, contentType);
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
