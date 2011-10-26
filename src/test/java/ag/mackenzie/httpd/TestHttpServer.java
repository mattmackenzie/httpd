package ag.mackenzie.httpd;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestHttpServer extends TestCase {
	private HttpServer server;
	
	
	public void setUp() throws Exception {
		server = new HttpServer();
		server.start();
	}

	public void testEcho() {
		String toEcho = Long.toString(System.currentTimeMillis());
		try {
			URL url = new URL("http://localhost:8900/sys/services/EchoService?echo=" + toEcho);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			InputStream stream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String echoed = reader.readLine();
			assertTrue(toEcho.equals(echoed));
		} catch (MalformedURLException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}
	
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestHttpServer.class );
    }
	public void tearDown() throws Exception {
		server.stopServer();
	}

}
