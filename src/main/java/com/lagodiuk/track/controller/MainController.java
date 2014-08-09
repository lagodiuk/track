package com.lagodiuk.track.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lagodiuk.track.storage.Storage;

@Controller
public class MainController {

	@RequestMapping("/ping")
	@ResponseBody
	public String ping() {
		return "OK";
	}

	@RequestMapping(value = "/track", method = {RequestMethod.POST})
	@ResponseBody
	public String getAttentionDistribution(@RequestBody TrackingInfo trackingInfo) {
		Storage.save(trackingInfo);
		return "Ok";
	}

}
