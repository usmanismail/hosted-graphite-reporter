package com.techtraits.metrics;

import com.yammer.metrics.core.MetricName;

class MetricRecord {

	private MetricName name;
	private String postFix;
	private String value;
	private long timeStamp;

	public String getPostFix() {
		if (postFix != null && postFix.length() > 0) {
			return "." + postFix;
		} else {
			return "";
		}
	}

	public MetricRecord(MetricName name, String postFix, String value, long timestamp) {
		this.name = name;
		this.postFix = postFix;
		this.value = value;
		this.timeStamp = timestamp;
	}

	public MetricName getName() {
		return name;
	}

	public void setName(MetricName name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public void setPostFix(String postFix) {
		this.postFix = postFix;
	}

}
