package com.lagodiuk.track;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlets.ProxyServlet;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.lagodiuk.track.proxy.HtmlTrackingFilter;

/**
 * https://gist.github.com/jponge/1752767
 */
public class Main {

	public static void main(String... args) throws Exception {
		launchBackend();
		launchProxy();
	}

	private static void launchProxy() throws Exception, InterruptedException {
		Server server = new Server(8080);

		ServletHandler servletHandler = new ServletHandler();
		servletHandler.addServletWithMapping(ProxyServlet.class, "/*");
		servletHandler.addFilterWithMapping(new FilterHolder(new HtmlTrackingFilter()), "/*", EnumSet.allOf(DispatcherType.class));

		server.setHandler(servletHandler);
		server.start();
		server.join();
	}

	private static void launchBackend() {
		new Thread() {
			@Override
			public void run() {
				new ClassPathXmlApplicationContext("app.xml");
			};
		}.start();
	}
}
