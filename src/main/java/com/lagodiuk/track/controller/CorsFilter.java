package com.lagodiuk.track.controller;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

// http://stackoverflow.com/questions/16190699/automatically-add-header-to-every-response/16191770#16191770
public class CorsFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		// http://stackoverflow.com/questions/4508198/how-to-use-type-post-in-jsonp-ajax-call/4528304#4528304
		// http://stackoverflow.com/questions/4508198/how-to-use-type-post-in-jsonp-ajax-call/17722058#17722058
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		response.setHeader("Access-Control-Allow-Methods", "POST");
		response.setHeader("Access-Control-Max-Age", "1000");

		filterChain.doFilter(request, response);
	}

}