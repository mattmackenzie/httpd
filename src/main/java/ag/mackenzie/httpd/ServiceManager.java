package ag.mackenzie.httpd;

import ag.mackenzie.httpd.services.EchoService;

public class ServiceManager {
	
	public static Service resolveService(String path) throws ServiceException {
		if (path.contains("/services/echo")) {
			return new EchoService();
		} else {
			throw new ServiceException("Service for " + path + " not registered.");
		}
	}
}
