package ag.mackenzie.httpd;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
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
			logger.severe(exception.getMessage());
		}
		
		if (headers != null && headers.size() > 0) {
			String[] directive = headers.get(0).split(" ");
			method = directive[0];
			path = directive[1];
		}
		else {
			logger.fine("Strange, the headers are null or 0 length. Returning.");
			return;
		}
		
		if (method.equalsIgnoreCase("GET")) {
			handleHttpGet(headers, path);
		}
		else {
			handleUnsupported(method);
		}
	}

	private void handleUnsupported(String method) {
		HTTPException httpEx = new HTTPException(HTTPResponseCode.UNIMPLEMENTED, method + " is not implemented.");

		String headers = getResponseHeaders(httpEx.getResponseCode(), httpEx.getMessage().length(), "text/html");
		try {
			output.writeBytes(headers + httpEx.getMessage());
			socket.close();
			socketKiller.cancel();
		} catch (IOException exception) {
			logger.info("Failed to send 501. " + exception.getMessage());
		}
	}
	
	private void handleFileNotFound(String path) {
		HTTPException httpEx = new HTTPException(HTTPResponseCode.FILE_NOT_FOUND, path + " was not found on this server.");
		
		String headers = getResponseHeaders(httpEx.getResponseCode(), httpEx.toString().length(), "text/html");
		try {
			output.writeBytes(headers + httpEx.getMessage());
			socket.close();
			socketKiller.cancel();
		} catch (IOException exception) {
			logger.info("Failed to send 404. " + exception.getMessage());
		}
	}
	private void handleHttpGet(LinkedList<String> headers, String path) {
		
		// handle default path.
		
		if (path.endsWith("/")) {
			path = path + "index.html";
		}
		
		File requestedFile = new File(webRoot + path);
		long contentLength = 0L;
		String contentType = guessMimeType(path);
		
		if (path.startsWith("/echo.svc")) {
			// special echo service
			handleEcho(path);
			return;
		}
		
		if (requestedFile.exists() && requestedFile.canRead()) {
			logger.info("Thread " + getId() + " Serving " + path);
			contentLength = requestedFile.length();
			try {		
				output.writeBytes(getResponseHeaders(HTTPResponseCode.OK, contentLength, contentType));
				InputStream requestedInStream = new FileInputStream(requestedFile);
				while (true) {
					int b = requestedInStream.read();
					if (b == -1) {
						output.flush();
						break;
					}
					output.write(b);
				}
				requestedInStream.close();
				
			} catch (IOException exception) {
				logger.throwing(HttpConnectionHandler.class.getName(), "handleRequest/handleHttpGet", exception);
			}
		}
		else {
			handleFileNotFound(path);
		}
	}

	private void handleEcho(String path) {
		logger.info("Invoking echo service.");
		String[] urlParts = path.split("\\?");
	    if (urlParts.length > 1) {
	        String query = urlParts[1];
	        for (String param : query.split("&")) {
	            String pair[] = param.split("=");
	            String key = "";
	            String value = "";
				try {
					key = URLDecoder.decode(pair[0], "UTF-8");
					value = URLDecoder.decode(pair[1], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					logger.throwing(getName(), "echo", e);
				}
	            
	            if (key.equalsIgnoreCase("echo")) {
	            	try {
						output.writeBytes(getResponseHeaders(HTTPResponseCode.OK, value.length(), "text/html"));
						output.writeBytes(value);
					} catch (IOException e) {
						logger.throwing(getName(), "echo", e);
					}
	            }
	        }
	    }
	}

	private String getResponseHeaders(HTTPResponseCode code, long contentLength, String contentType) {
		// I am assuming I always am dealing in HTTP 1.1 and keep-alive...
		StringBuilder headers = new StringBuilder();
		headers.append(code.toString()).append("\r\n");
		headers.append("Content-Type: " + contentType + "\r\n").append("Content-Length: " + contentLength + "\r\n");
		headers.append("Connection: keep-alive\r\n").append("Keep-Alive: timeout=20\r\n").append("Server: MattServe2011\r\n\r\n");
		return headers.toString();
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
