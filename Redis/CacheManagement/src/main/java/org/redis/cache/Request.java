package org.redis.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {
	
	
	private String pageRequestURL;
	
	private String pageContent;

	@JsonProperty
	public String getPageRequestURL() {
		return pageRequestURL;
	}

	public void setPageRequestURL(String pageRequestURL) {
		this.pageRequestURL = pageRequestURL;
	}

	@JsonProperty
	public String getPageContent() {
		return pageContent;
	}

	public void setPageContent(String pageContent) {
		this.pageContent = pageContent;
	}
	
}
