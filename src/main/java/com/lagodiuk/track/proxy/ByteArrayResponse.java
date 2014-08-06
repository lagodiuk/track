package com.lagodiuk.track.proxy;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

final class ByteArrayResponse extends HttpServletResponseWrapper {
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	ByteArrayResponse(HttpServletResponse response) {
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

	public ByteArrayOutputStream getBaos() throws Exception {
		this.baos.flush();
		return this.baos;
	}
}