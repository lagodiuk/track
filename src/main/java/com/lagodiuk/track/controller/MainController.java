package com.lagodiuk.track.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
		System.out.println(trackingInfo);
		return "Ok";
	}

}
