package ag.mackenzie.httpd;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import ag.mackenzie.httpd.services.GenericWebService;

public class ServiceManager {
	private HashMap<String, Class> services;
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
		/*
		services = new HashMap<String, Class>();
		String path = servicePackage.replace('.', '/');
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		
		try {
			Enumeration<URL> resources;
			resources = classLoader.getResources(path);
			List<File> dirs = new ArrayList<File>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				dirs.add(new File(resource.getFile()));
			}
			ArrayList<Class> classes = new ArrayList<Class>();
			for (File directory : dirs) {
				classes.addAll(findClasses(directory, "ag.mackenzie.httpd.services"));
			}
			for (Class cls : classes) {
				String key = cls.getName();
				if (key.lastIndexOf('.') > 0) {
					key = key.substring(key.lastIndexOf('.')+1);
				}
				System.out.println("key: " + key + " class: " + cls.getName());
				services.put(key, cls);
			}
		} catch (IOException e) {
			throw new ServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new ServiceException(e);
		}
		*/

	}
	
	public Service resolveService(String path) throws ServiceException {
	
		if (path.contains("/sys/services/")) {
			String[] pathEls = path.split("/");
			String key = pathEls[pathEls.length-1];
			if (key.indexOf('?') > 0) {
				key = key.split("\\?")[0];
			}
			//System.out.println(key);
			//Class svc = services.get(key);
			try {
				Class cls = Class.forName(this.servicePackage + "." + key);
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
	
	 private List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
	        List<Class> classes = new ArrayList<Class>();
	        if (!directory.exists()) {
	            return classes;
	        }
	        File[] files = directory.listFiles();
	        for (File file : files) {
	            if (file.isDirectory()) {
	                assert !file.getName().contains(".");
	                classes.addAll(findClasses(file, packageName + "." + file.getName()));
	            } else if (file.getName().endsWith(".class")) {
	                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
	            }
	        }
	        return classes;
	    }
}
