package ag.mackenzie.httpd;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class HTTPResponse {
	private DataOutputStream output;

	public HTTPResponse(DataOutputStream output) {
		this.output = output;
	}
	
	public void writeHeaders(HTTPResponseCode code, long contentLength, String contentType, boolean keepAlive) throws IOException {
		StringBuilder headers = new StringBuilder();
		headers.append(code.toString()).append("\r\n");
		headers.append("Content-Type: " + contentType + "\r\n").append("Content-Length: " + contentLength + "\r\n");
		if (keepAlive) {
			headers.append("Connection: keep-alive\r\n").append("Keep-Alive: timeout=20\r\n");
		}
		headers.append("Server: MattServe2011\r\n\r\n");
		output.writeBytes(headers.toString());
	}
	
	public void writeFile(File file) throws IOException, FileNotFoundException {
		InputStream requestedInStream;
		requestedInStream = new FileInputStream(file);
	
		while (true) {
			int b = requestedInStream.read();
			if (b == -1) {
				output.flush();
				break;
			}
			output.write(b);
		}
		requestedInStream.close();
	}
	
	public void writeString(String toWrite) throws IOException {
		output.writeBytes(toWrite);
	}
}
