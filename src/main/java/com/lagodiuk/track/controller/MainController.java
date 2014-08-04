package com.lagodiuk.track.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlScript;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;

@Controller
public class MainController {

	@RequestMapping("/ping")
	@ResponseBody
	public String ping() {
		return "OK";
	}

	@RequestMapping("/track")
	@ResponseBody
	public String url(@RequestParam String url) throws Exception {
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		HtmlPage page = webClient.getPage(url);

		this.wrapTextNodes(page.getBody(), page, 0);

		HtmlButton button = (HtmlButton) page.createElement("button");
		button.setAttribute("style", "position:fixed; top:0px;left:0px; z-index:200");
		button.setTextContent("What do I see?");
		page.getBody().appendChild(button);

		HtmlScript jquery = (HtmlScript) page.createElement("script");
		jquery.setAttribute("src", "/s/jquery.2.1.0.min.js");
		page.getHead().appendChild(jquery);

		// http://stackoverflow.com/questions/9271747/can-i-detect-the-user-viewable-area-on-the-browser
		HtmlScript trackScript = (HtmlScript) page.createElement("script");
		// http://jsfiddle.net/Ua37w/8/
		trackScript.setAttribute("src", "/s/track.js");
		page.getHead().appendChild(trackScript);

		return "<!DOCTYPE html>" + page.asXml();
	}

	private int wrapTextNodes(DomNode node, HtmlPage page, int counter) throws Exception {
		if (node instanceof HtmlScript) {
			HtmlScript htmlSript = (HtmlScript) node;
			htmlSript.setAttribute("src", page.getFullyQualifiedUrl(htmlSript.getSrcAttribute()).toString());
			return counter;
		}
		if (node.getNodeType() == DomNode.TEXT_NODE) {
			String textContent = node.getTextContent();
			if (textContent.trim().isEmpty()) {
				return counter;
			}
			node.setTextContent("");

			for (String chunk : this.splitEqually(textContent, 200)) {
				HtmlSpan span = (HtmlSpan) page.createElement("span");
				span.setAttribute("class", "track");
				span.setAttribute("counter", counter + "");
				System.out.println(counter + "\t" + chunk);
				span.setTextContent(chunk);
				node.appendChild(span);
				counter++;
			}

			return counter;
		}

		for (DomNode n : node.getChildren()) {
			counter = this.wrapTextNodes(n, page, counter);
		}
		return counter;
	}

	// http://stackoverflow.com/a/3760193/653511
	public List<String> splitEqually(String text, int size) {
		// Give the list the right capacity to start with. You could use an
		// array
		// instead if you wanted.
		List<String> ret = new ArrayList<String>(((text.length() + size) - 1) / size);

		for (int start = 0; start < text.length(); start += size) {
			ret.add(text.substring(start, Math.min(text.length(), start + size)));
		}
		return ret;
	}

}
