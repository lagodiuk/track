package com.lagodiuk.track.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

class ByteArrayServletStream extends ServletOutputStream {

	private ByteArrayOutputStream baos;

	ByteArrayServletStream(ByteArrayOutputStream baos) {
		this.baos = baos;
	}

	@Override
	public void write(int param) throws IOException {
		this.baos.write(param);
	}
}