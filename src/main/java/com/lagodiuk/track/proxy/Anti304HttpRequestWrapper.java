package com.lagodiuk.track.proxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * When the browser puts something in its cache, it also stores the
 * "Last-Modified" or "ETag" header from the server.
 *
 * The browser then sends a request with the "If-Modified-Since" or
 * "Unless-Match" header, telling the server to send a 304 if the content still
 * has that date or ETag.
 *
 * @see http://stackoverflow.com/questions/20978189/how-304-not-modified-works
 */
public class Anti304HttpRequestWrapper extends HttpServletRequestWrapper {

	private static final String UNLESS_MATCH_HEADER = "Unless-Match";

	private static final String IF_NONE_MATCH_HEADER = "If-None-Match";

	private static final String DEFAULT_VALUE = "123";

	public Anti304HttpRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getHeader(String name) {
		if (name.equals(IF_NONE_MATCH_HEADER) || name.equals(UNLESS_MATCH_HEADER)) {
			return DEFAULT_VALUE;
		}
		return super.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		if (name.equals(IF_NONE_MATCH_HEADER) || name.equals(UNLESS_MATCH_HEADER)) {
			return Collections.enumeration(Arrays.asList(DEFAULT_VALUE));
		}
		return super.getHeaders(name);
	}
}