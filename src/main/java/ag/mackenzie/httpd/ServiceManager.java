package ag.mackenzie.httpd;

import ag.mackenzie.httpd.services.EchoService;
import ag.mackenzie.httpd.services.HeaderEchoService;

public class ServiceManager {
	
	public static Service resolveService(String path) throws ServiceException {
		if (path.contains("/services/echo")) {
			return new EchoService();
		} else if (path.contains("/services/headers")) {
			return new HeaderEchoService();
		}
		else {
		
			throw new ServiceException("Service for " + path + " not registered.");
		}
	}
}
