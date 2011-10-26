package ag.mackenzie.httpd;

public interface Service {
	public void process(HTTPRequest request, HTTPResponse response) throws ServiceException;
}
