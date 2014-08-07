package com.lagodiuk.track.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.html.HtmlEncodingDetector;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationSupport;

final class HtmlTrackingFilter implements Filter {

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {

		final ByteArrayResponse wrappedResponse = new ByteArrayResponse((HttpServletResponse) response);
		final HttpServletRequest wrappedRequest = new Anti304HttpRequestWrapper((HttpServletRequest) request);

		try {
			chain.doFilter(wrappedRequest, wrappedResponse);
		} finally {
			// Example of usage of Continuation see in:
			// org.eclipse.jetty.servlets.GzipFilter
			Continuation continuation = ContinuationSupport.getContinuation(wrappedRequest);

			if (continuation.isSuspended() && continuation.isResponseWrapped()) {

				continuation.addContinuationListener(new ContinuationListener() {

					@Override
					public void onComplete(Continuation continuation) {
						try {
							HtmlTrackingFilter.this.process(wrappedRequest, response, wrappedResponse);
						} catch (Exception e) {
							e.printStackTrace();
							// TODO
						}
					}

					@Override
					public void onTimeout(Continuation continuation) {
						// TODO
					}
				});

			} else {
				try {
					this.process(wrappedRequest, response, wrappedResponse);
				} catch (Exception e) {
					e.printStackTrace();
					// TODO
				}
			}
		}
	}

	private void process(ServletRequest request, final ServletResponse response, final ByteArrayResponse wrappedResponse) throws Exception {

		byte[] bytes = wrappedResponse.getResponseBody();

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
	public void init(FilterConfig filterConfig) throws ServletException {
		// Auto-generated method stub
	}

	@Override
	public void destroy() {
		// Auto-generated method stub
	}
}