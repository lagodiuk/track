package com.lagodiuk.track.storage;

import java.util.HashMap;
import java.util.Map;

import com.lagodiuk.track.controller.TrackingInfo;

public class Storage {

	private static Map<String, Map<Integer, Double>> urlToAttentionDistribution = new HashMap<>();

	public static void save(TrackingInfo trackingInfo) {
		trackingInfo.normalize();

		String url = trackingInfo.getUrl();
		Map<Integer, Double> attentionDistribution = trackingInfo.getAttentionDistribution();

		Map<Integer, Double> existingDistribution = urlToAttentionDistribution.get(url);

		if (existingDistribution == null) {
			urlToAttentionDistribution.put(url, attentionDistribution);
		} else {
			for (Integer key : attentionDistribution.keySet()) {
				Double existingValue = existingDistribution.get(key);
				if (existingValue == null) {
					existingValue = 0.0;
				}
				existingDistribution.put(key, existingValue + attentionDistribution.get(key));
			}
		}

		System.out.println("Saved: " + url);
	}

}
