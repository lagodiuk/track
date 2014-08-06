package com.lagodiuk.track.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

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
		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			// TODO Auto-generated method stub

		}
		@Override
		public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {
			final ByteArrayResponse wrappedResponse = new ByteArrayResponse((HttpServletResponse) response);

			final HttpServletRequest hreq = new HttpServletRequestWrapper((HttpServletRequest) request) {
				@Override
				public String getHeader(String name) {
					if (name.equals("If-None-Match")) {
						return "123";
					}
					return super.getHeader(name);
				}

				@Override
				public Enumeration<String> getHeaders(String name) {
					// http://stackoverflow.com/questions/20978189/how-304-not-modified-works
					if (name.equals("If-None-Match")) {
						return Collections.enumeration(Arrays.asList("123"));
					}
					return super.getHeaders(name);
				}
			};

			try {
				chain.doFilter(hreq, wrappedResponse);
			} finally {
				// see: org.eclipse.jetty.servlets.GzipFilter

				Continuation continuation = ContinuationSupport.getContinuation(hreq);

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
								MyDumpFilter.this.process(hreq, response, wrappedResponse);
							} catch (Exception e) {
								e.printStackTrace();
								System.exit(0);
							}
						}
					});

				} else {
					try {
						this.process(hreq, response, wrappedResponse);
					} catch (Exception e) {
						throw new ServletException(e);
					}
				}
			}
		}

		private void process(ServletRequest request, final ServletResponse response, final ByteArrayResponse wrappedResponse) throws Exception {

			byte[] bytes = wrappedResponse.getBaos().toByteArray();

			HttpServletRequest hreq = (HttpServletRequest) request;
			HttpServletResponse hresp = (HttpServletResponse) response;

			String url = this.getFullURL(hreq);

			Metadata metadata = new Metadata();
			metadata.set(Metadata.LOCATION, url);
			Detector detector = TikaConfig.getDefaultConfig().getDetector();
			MediaType type = detector.detect(new ByteArrayInputStream(bytes), metadata);

			StringBuilder sb = new StringBuilder();
			sb.append(url).append("\n");
			sb.append("Request headers:").append("\n");
			Enumeration<String> en = hreq.getHeaderNames();
			while (en.hasMoreElements()) {
				String h = en.nextElement();
				sb.append(h).append("\t").append(hreq.getHeader(h)).append("\n");
			}
			sb.append("Response headers:").append("\n");
			for (String h : hresp.getHeaderNames()) {
				sb.append(h).append("\t").append(hresp.getHeaders(h)).append("\n");
			}
			sb.append(type.getType()).append(" ")
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

					String bytesString = new String(bytes);
					if (bytesString.toLowerCase().contains("<!doctype html>")) {

						HtmlEncodingDetector encodingDetector = new HtmlEncodingDetector();
						Charset charset = encodingDetector.detect(new ByteArrayInputStream(bytes), metadata);
						if (charset == null) {
							charset = Charset.forName("utf-8");
						}

						sb.append("\n").append(new String(bytes, charset)).append("\n");
					}
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

	public static void main(String... args) throws Exception {
		Server server = new Server(8080);

		ServletHandler servletHandler = new ServletHandler();
		servletHandler.addServletWithMapping(ProxyServlet.class, "/*");
		servletHandler.addFilterWithMapping(new FilterHolder(new MyDumpFilter()), "/*", EnumSet.allOf(DispatcherType.class));

		server.setHandler(servletHandler);
		server.start();
		server.join();
	}
}
