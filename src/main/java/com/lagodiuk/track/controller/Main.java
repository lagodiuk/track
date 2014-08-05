package com.lagodiuk.track.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlets.ProxyServlet;

/**
 * https://gist.github.com/jponge/1752767
 */
public class Main {

	private static final class MyDumpFilter implements Filter {
		private static final class ByteArrayResponse extends HttpServletResponseWrapper {
			private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			private ByteArrayResponse(HttpServletResponse response) {
				super(response);
			}
			@Override
			public PrintWriter getWriter() {
				return new PrintWriter(this.baos);
			}
			@Override
			public ServletOutputStream getOutputStream() {
				return new ByteArrayServletStream(this.baos);
			}

			public ByteArrayOutputStream getBaos() {
				return this.baos;
			}
		}
		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			// TODO Auto-generated method stub

		}
		@Override
		public void doFilter(ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {
			final ByteArrayResponse wrappedResponse = new ByteArrayResponse((HttpServletResponse) response);

			try {
				chain.doFilter(request, wrappedResponse);
			} finally {
				// see: org.eclipse.jetty.servlets.GzipFilter

				Continuation continuation = ContinuationSupport.getContinuation(request);
				if (continuation.isSuspended() && continuation.isResponseWrapped()) {

					continuation.addContinuationListener(new ContinuationListener() {
						@Override
						public void onTimeout(Continuation continuation) {
							System.out.println("TIMEOUT");
							System.exit(0);
						}

						@Override
						public void onComplete(Continuation continuation) {
							byte[] bytes = wrappedResponse.getBaos().toByteArray();
							try {
								for (String s : wrappedResponse.getHeaderNames()) {
									System.out.println(s + "\t" + wrappedResponse.getHeader(s));
								}
								String contentType = wrappedResponse.getContentType();
								System.out.println(contentType);
								if ((contentType != null) && contentType.contains("html")) {
									System.out.println(new String(bytes, wrappedResponse.getCharacterEncoding()));
								}
								response.getOutputStream().write(bytes);
							} catch (Exception e) {
								e.printStackTrace();
								System.exit(0);
							}
						}
					});

				} else {
					byte[] bytes = wrappedResponse.getBaos().toByteArray();
					String contentType = wrappedResponse.getContentType();
					System.out.println(contentType);
					if ((contentType != null) && contentType.contains("html")) {
						System.out.println(new String(bytes, wrappedResponse.getCharacterEncoding()));
					}
					response.getOutputStream().write(bytes);
				}
			}
		}
		@Override
		public void destroy() {
			// TODO Auto-generated method stub

		}
	}

	public static class MyProxyServlet extends ProxyServlet {
		@Override
		public void init(ServletConfig config) throws ServletException {
			super.init(config);
			System.out.println(">> init done !");
		}

		@Override
		public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
			System.out.println(">>> got a request ! " + req.getServerName());
			super.service(req, res);
		}
	}

	public static void main(String... args) throws Exception {
		Server server = new Server(8080);

		ServletHandler servletHandler = new ServletHandler();
		servletHandler.addServletWithMapping(MyProxyServlet.class, "/*");
		servletHandler.addFilterWithMapping(new FilterHolder(new MyDumpFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));

		server.setHandler(servletHandler);
		server.start();
		server.join();
	}

	private static class ByteArrayServletStream extends ServletOutputStream {

		private ByteArrayOutputStream baos;

		ByteArrayServletStream(ByteArrayOutputStream baos) {
			this.baos = baos;
		}

		@Override
		public void write(int param) throws IOException {
			this.baos.write(param);
		}
	}
}
