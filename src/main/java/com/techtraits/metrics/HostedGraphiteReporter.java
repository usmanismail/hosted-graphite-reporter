package com.techtraits.metrics;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.util.Base64;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;

/**
 * This class will use the HTTP Interface to send individual metrics to <a href="http://hostedgraphite.com/docs/">Hosted Graphite</a>.
 * 
 * @author Usman Ismail
 * 
 */
public class HostedGraphiteReporter {

	private final URL hostedGraphiteUrl;
	private static final Logger log = Logger.getLogger(HostedGraphiteReporter.class.getName());
	private final URLFetchService fetcher;
	private final HTTPHeader authHeader;
	private static final double requestTimeout = 60d; // 60 seconds

	@Inject
	public HostedGraphiteReporter(@Named("hosted.graphite.key") String hostedGraphiteSecret, @Named("hosted.graphite.url") String hostedGraphiteUrl,
			URLFetchService fetcher) throws UnsupportedEncodingException, MalformedURLException {
		String authHeaderString = "Basic " + Base64.encodeBase64String((hostedGraphiteSecret + ":foo").getBytes("ISO-8859-1"));
		authHeader = new HTTPHeader("Authorization", authHeaderString);
		this.fetcher = fetcher;
		this.hostedGraphiteUrl = new URL(hostedGraphiteUrl);
	}

	/**
	 * @param name
	 * @param meter
	 * @param timeStamp
	 */
	public void reportMeterView(MetricName name, Meter meter, Long timeStamp) throws Exception {
		postMetrics(createMeterRecords(name, meter, timeStamp));
	}

	/**
	 * @param name
	 * @param counter
	 * @param timeStamp
	 */
	public void reportCounterView(MetricName name, Counter counter, Long timeStamp) throws Exception {
		postMetrics(createCounterRecords(name, counter, timeStamp));
	}

	/**
	 * @param name
	 * @param histogram
	 * @param timeStamp
	 */
	public void reportHistogramView(MetricName name, Histogram histogram, Long timeStamp) throws Exception {
		postMetrics(createHistogramRecords(name, histogram, timeStamp));
	}

	/**
	 * @param name
	 * @param timer
	 * @param timeStamp
	 */
	public void reportTimerView(MetricName name, Timer timer, Long timeStamp) throws Exception {
		postMetrics(createTimerRecords(name, timer, timeStamp));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void reportMetrics(Map<MetricName, Metric> metrics, Long timestamp) throws IOException {

		List<MetricRecord> metricRecords = new ArrayList<MetricRecord>();
		for (MetricName metricName : metrics.keySet()) {
			Metric metric = metrics.get(metricName);
			if (metric instanceof Counter) {
				metricRecords.addAll(createCounterRecords(metricName, (Counter) metric, timestamp));
			} else if (metric instanceof Meter) {
				metricRecords.addAll(createMeterRecords(metricName, (Meter) metric, timestamp));
			} else if (metric instanceof Histogram) {
				metricRecords.addAll(createHistogramRecords(metricName, (Histogram) metric, timestamp));
			} else if (metric instanceof Timer) {
				metricRecords.addAll(createTimerRecords(metricName, (Timer) metric, timestamp));
			} else if (metric instanceof Gauge) {
				metricRecords.addAll(createGaugeRecords(metricName, (Gauge) metric, timestamp));
			}
		}
		postMetrics(metricRecords);

	}

	/**
	 * @param name
	 * @param gauge
	 * @param timeStamp
	 */
	public <T> void reportGaugeView(MetricName name, Gauge<T> gauge, Long timeStamp) throws Exception {
		postMetrics(createGaugeRecords(name, gauge, timeStamp));
	}

	private List<MetricRecord> createMeterRecords(MetricName name, Meter meter, Long timeStamp) {
		List<MetricRecord> metrics = new ArrayList<MetricRecord>();
		metrics.add(new MetricRecord(name, "count", String.valueOf(meter.getCount()), timeStamp));
		metrics.add(new MetricRecord(name, "meanRate", String.valueOf(meter.getMeanRate()), timeStamp));
		metrics.add(new MetricRecord(name, "oneMinuteRate", String.valueOf(meter.getOneMinuteRate()), timeStamp));
		metrics.add(new MetricRecord(name, "fiveMinuteRate", String.valueOf(meter.getFiveMinuteRate()), timeStamp));
		metrics.add(new MetricRecord(name, "fifteenMinuteRate", String.valueOf(meter.getFifteenMinuteRate()), timeStamp));
		return metrics;
	}

	private List<MetricRecord> createCounterRecords(MetricName name, Counter counter, Long timeStamp) {
		List<MetricRecord> metrics = new ArrayList<MetricRecord>();
		metrics.add(new MetricRecord(name, "count", String.valueOf(counter.getCount()), timeStamp));
		return metrics;
	}

	private List<MetricRecord> createHistogramRecords(MetricName name, Histogram histogram, Long timeStamp) {
		List<MetricRecord> metrics = new ArrayList<MetricRecord>();
		metrics.add(new MetricRecord(name, "count", String.valueOf(histogram.getCount()), timeStamp));
		metrics.add(new MetricRecord(name, "mean", String.valueOf(histogram.getMean()), timeStamp));
		metrics.add(new MetricRecord(name, "median", String.valueOf(histogram.getSnapshot().getMedian()), timeStamp));

		metrics.add(new MetricRecord(name, "95percentile", String.valueOf(histogram.getSnapshot().get95thPercentile()), timeStamp));
		metrics.add(new MetricRecord(name, "98percentile", String.valueOf(histogram.getSnapshot().get98thPercentile()), timeStamp));
		metrics.add(new MetricRecord(name, "99percentile", String.valueOf(histogram.getSnapshot().get99thPercentile()), timeStamp));

		return metrics;
	}

	private List<MetricRecord> createTimerRecords(MetricName name, Timer timer, Long timeStamp) {
		List<MetricRecord> metrics = new ArrayList<MetricRecord>();
		metrics.addAll(createMeterRecords(name, timer.getMeter(), timeStamp));
		metrics.add(new MetricRecord(name, "mean", String.valueOf(timer.getMean()), timeStamp));
		metrics.add(new MetricRecord(name, "median", String.valueOf(timer.getSnapshot().getMedian()), timeStamp));
		metrics.add(new MetricRecord(name, "95percentile", String.valueOf(timer.getSnapshot().get95thPercentile()), timeStamp));
		metrics.add(new MetricRecord(name, "98percentile", String.valueOf(timer.getSnapshot().get98thPercentile()), timeStamp));
		metrics.add(new MetricRecord(name, "99percentile", String.valueOf(timer.getSnapshot().get99thPercentile()), timeStamp));
		return metrics;
	}

	private <T> List<MetricRecord> createGaugeRecords(MetricName name, Gauge<T> gauge, Long timeStamp) {
		List<MetricRecord> metrics = new ArrayList<MetricRecord>();
		metrics.add(new MetricRecord(name, "value", String.valueOf(gauge.getValue()), timeStamp));
		return metrics;
	}

	protected String sanitizeName(MetricName name) {

		final StringBuilder sb = new StringBuilder().append(name.getDomain()).append('.').append(name.getType()).append('.');
		if (name.hasScope()) {
			sb.append(name.getScope()).append('.');
		}
		sb.append(name.getName());
		return sanitizeString(sb.toString());
	}

	protected String sanitizeString(String s) {
		return s.replace(' ', '-');
	}

	public void postMetrics(List<MetricRecord> metrics) throws IOException {
		HTTPRequest request = new HTTPRequest(hostedGraphiteUrl, HTTPMethod.POST);
		request.getFetchOptions().setDeadline(requestTimeout);
		request.setHeader(authHeader);
		StringBuilder metricsPayload = new StringBuilder();
		for (MetricRecord metricRecord : metrics) {
			metricsPayload.append(SystemProperty.applicationId.get()).append('.').append(sanitizeName(metricRecord.getName()))
					.append(metricRecord.getPostFix()).append(" ").append(metricRecord.getValue()).append(" ").append(metricRecord.getTimeStamp()).append("\n");
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, metricsPayload.toString());
		}

		request.setPayload(metricsPayload.toString().getBytes());
		HTTPResponse response = fetcher.fetch(request);
		if (response.getResponseCode() != 202) {
			log.severe("Unable to send update error code: " + response.getResponseCode() + " Text: " + new String(response.getContent()));
		}

	}

}
