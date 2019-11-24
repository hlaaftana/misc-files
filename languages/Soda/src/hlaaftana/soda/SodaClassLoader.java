package hlaaftana.soda;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class SodaClassLoader extends URLClassLoader {
	public void addURL(URL url) {
		super.addURL(url);
	}

	public SodaClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public SodaClassLoader(URL[] urls) {
		super(urls);
	}

	public SodaClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}
}
