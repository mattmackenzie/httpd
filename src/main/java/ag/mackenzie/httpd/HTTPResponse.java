package ag.mackenzie.httpd;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

// I am using this one class from someone else.  Copyright intact.
import org.apache.axis.transport.http.ChunkedOutputStream;

public class HTTPResponse {
	private OutputStream output;

	public HTTPResponse(OutputStream output) {
		this.output = output;
	}
	
	public void writeHeaders(HTTPResponseCode code, long contentLength, String contentType, boolean keepAlive) throws IOException {
		StringBuilder headers = new StringBuilder();
		headers.append(code.toString()).append("\r\n");
		headers.append("Content-Type: " + contentType + "\r\n");
		if (keepAlive) {
			headers.append("Connection: keep-alive\r\n").append("Keep-Alive: timeout=20\r\n").append("Transfer-Encoding: chunked\r\n");
		}
		headers.append("Server: MattServe2011\r\n\r\n");
		DataOutputStream dataOut = new DataOutputStream(output);
		dataOut.writeBytes(headers.toString());
	}
	
	
	public void writeFile(File file) throws IOException, FileNotFoundException {	
		InputStream requestedInStream;
		requestedInStream = new FileInputStream(file);
		ChunkedOutputStream ostream = new ChunkedOutputStream(output);
		byte[] buffer = new byte[32];
		
		synchronized(buffer) {
			while (true) {
				int read = requestedInStream.read(buffer);
				if (read == -1) {
					ostream.eos();
					break;
				}
				ostream.write(buffer);
			}
		}
		requestedInStream.close();
		
	}
	
	public void writeString(String toWrite) throws IOException {
		ChunkedOutputStream cos = new ChunkedOutputStream(output);
		PrintStream pstream = new PrintStream(cos);
		pstream.print(toWrite);
		pstream.flush();
		cos.eos();
		
	}
}
