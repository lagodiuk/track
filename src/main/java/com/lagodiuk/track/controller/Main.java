package com.lagodiuk.track.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.zip.GZIPInputStream;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.html.HtmlEncodingDetector;
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
		public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {
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
							try {
								MyDumpFilter.this.process(request, response, wrappedResponse);
							} catch (Exception e) {
								e.printStackTrace();
								System.exit(0);
							}
						}
					});

				} else {
					this.process(request, response, wrappedResponse);
				}
			}
		}

		private void process(ServletRequest request, final ServletResponse response, final ByteArrayResponse wrappedResponse)
				throws UnsupportedEncodingException, IOException {

			byte[] bytes = wrappedResponse.getBaos().toByteArray();

			HttpServletRequest hreq = (HttpServletRequest) request;
			HttpServletResponse hresp = (HttpServletResponse) response;

			// String contentType = wrappedResponse.getHeader("Content-Type");
			// if ((contentType != null) && contentType.contains("text/html")) {
			// String charset = "utf-8";
			// if (contentType.matches("^.*charset=(.+)$")) {
			// charset = contentType.replaceAll("^.*charset=(.+)$", "$1");
			// }
			//
			// byte[] decodedBytes = bytes;
			// String contentEncoding =
			// wrappedResponse.getHeader("Content-Encoding");
			// if ((contentEncoding != null) &&
			// contentEncoding.contains("gzip")) {
			// GZIPInputStream gzip = new GZIPInputStream(new
			// ByteArrayInputStream(bytes));
			// ByteArrayOutputStream tmp = new ByteArrayOutputStream();
			// int c;
			// while ((c = gzip.read()) != -1) {
			// tmp.write(c);
			// }
			// decodedBytes = tmp.toByteArray();
			// }
			//
			// System.out.println(new String(decodedBytes, charset));
			// }

			String url = this.getFullURL(hreq);

			Metadata metadata = new Metadata();
			metadata.set(Metadata.LOCATION, url);
			Detector detector = TikaConfig.getDefaultConfig().getDetector();
			MediaType type = detector.detect(new ByteArrayInputStream(bytes), metadata);

			StringBuilder sb = new StringBuilder();
			sb.append(url).append("\n")
					.append(type.getType()).append(" ")
					.append(type.getSubtype()).append(" ")
					.append(type.getBaseType()).append("\n");

			if (type.getSubtype().contains("html")) {
				HtmlEncodingDetector encodingDetector = new HtmlEncodingDetector();
				Charset charset = encodingDetector.detect(new ByteArrayInputStream(bytes), metadata);
				if (charset == null) {
					charset = Charset.forName("utf-8");
				}

				sb.append("\n").append(new String(bytes, charset)).append("\n");
			}

			if (type.getSubtype().equals("x-gzip") || type.getSubtype().equals("octet-stream")) {
				try {
					byte[] ungzipped = this.unGzip(bytes);
					type = detector.detect(new ByteArrayInputStream(ungzipped), metadata);

					sb.append(type.getType()).append(" ")
							.append(type.getSubtype()).append(" ")
							.append(type.getBaseType()).append("\n");

					if (type.getSubtype().contains("html")) {
						HtmlEncodingDetector encodingDetector = new HtmlEncodingDetector();
						Charset charset = encodingDetector.detect(new ByteArrayInputStream(ungzipped), metadata);
						if (charset == null) {
							charset = Charset.forName("utf-8");
						}

						sb.append("\n").append(new String(ungzipped, charset)).append("\n");
					}
				} catch (Exception e) {
					// TODO
				}
			}

			System.out.println(sb.toString());

			response.getOutputStream().write(bytes);
		}

		private byte[] unGzip(byte[] bytes) throws Exception {
			GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
			ByteArrayOutputStream tmp = new ByteArrayOutputStream();
			int c;
			while ((c = gzip.read()) != -1) {
				tmp.write(c);
			}
			byte[] decodedBytes = tmp.toByteArray();
			return decodedBytes;
		}

		private String getFullURL(HttpServletRequest request) {
			StringBuffer requestURL = request.getRequestURL();
			String queryString = request.getQueryString();

			if (queryString == null) {
				return requestURL.toString();
			} else {
				return requestURL.append('?').append(queryString).toString();
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
			// System.out.println(">> init done !");
		}

		@Override
		public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
			// System.out.println(">>> got a request ! " + req.getServerName());
			super.service(req, res);
		}
	}

	public static void main(String... args) throws Exception {
		Server server = new Server(8080);

		ServletHandler servletHandler = new ServletHandler();
		servletHandler.addServletWithMapping(MyProxyServlet.class, "/*");
		servletHandler.addFilterWithMapping(new FilterHolder(new MyDumpFilter()), "/*", EnumSet.allOf(DispatcherType.class));

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
