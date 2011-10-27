package ag.mackenzie.httpd;

import ag.mackenzie.httpd.services.GenericWebService;

public class ServiceManager {
	private String servicePackage;
	private static ServiceManager instance;
	
	public static ServiceManager getInstance(String servicePackage) throws ServiceException {
		if (ServiceManager.instance == null) {
			ServiceManager.instance = new ServiceManager(servicePackage);
		}
		return ServiceManager.instance;
	}
	
	private ServiceManager(String servicePackage) throws ServiceException {
		this.servicePackage = servicePackage;
	}
	
	public Service resolveService(String path) throws ServiceException {
	
		if (path.contains("/sys/services/")) {
			String[] pathEls = path.split("/");
			String key = pathEls[pathEls.length-1];
			if (key.indexOf('?') > 0) {
				key = key.split("\\?")[0];
			}
			try {
				Class<?> cls = Class.forName(this.servicePackage + "." + key);
				return (Service)cls.newInstance();
			} catch (InstantiationException e) {
				throw new ServiceException(e);
			} catch (IllegalAccessException e) {
				throw new ServiceException(e);
			} catch (ClassNotFoundException e) {
				throw new ServiceException("A Class for service: " + key + " was not found in " + this.servicePackage);
			} 
		}
		else {
			return new GenericWebService();
		}
	}
}
