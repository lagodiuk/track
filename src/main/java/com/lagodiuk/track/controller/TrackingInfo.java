package com.lagodiuk.track.controller;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingInfo {

	private String url;

	private Map<Integer, Double> attentionDistribution;

	public String getUrl() {
		return this.url;
	}

	public Map<Integer, Double> getAttentionDistribution() {
		return this.attentionDistribution;
	}

	@Override
	public String toString() {
		return "TrackingInfo [url=" + this.url + ", attentionDistribution=" + this.attentionDistribution + "]";
	}

}
