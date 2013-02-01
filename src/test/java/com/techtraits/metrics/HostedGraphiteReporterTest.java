package com.techtraits.metrics;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.techtraitrs.metrics.HostedGraphiteReporter;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;

public class HostedGraphiteReporterTest {

	@Test(groups = "automated")
	public void testCounter() throws Exception {
		URLFetchService fetcher = mock(URLFetchService.class);
		HTTPResponse resp = mock(HTTPResponse.class);
		ArgumentCaptor<HTTPRequest> reqCapture = ArgumentCaptor.forClass(HTTPRequest.class);

		when(fetcher.fetch(any(HTTPRequest.class))).thenReturn(resp);
		when(resp.getResponseCode()).thenReturn(202);
		when(resp.getContent()).thenReturn("WOOHOO".getBytes());

		HostedGraphiteReporter reporter = new HostedGraphiteReporter("RosesAreRed", "http://www.hostedgraphite.com", fetcher);

		// Create the counter view
		Counter c = Metrics.newCounter(HostedGraphiteReporterTest.class, "CounterTest");
		c.inc();


		reporter.reportCounterView(new MetricName("test", "counter", "testCounter"), c, 1000000l);

		verify(fetcher).fetch(reqCapture.capture());
		String captured = new String(reqCapture.getValue().getPayload());
		Assert.assertTrue(captured.startsWith("null.test.counter.testCounter.count 1 1000000"));

	}

	@Test(groups = "automated")
	public void testCounterError() throws Exception {
		URLFetchService fetcher = mock(URLFetchService.class);
		HTTPResponse resp = mock(HTTPResponse.class);
		ArgumentCaptor<HTTPRequest> reqCapture = ArgumentCaptor.forClass(HTTPRequest.class);

		when(fetcher.fetch(any(HTTPRequest.class))).thenReturn(resp);
		when(resp.getResponseCode()).thenReturn(400);
		when(resp.getContent()).thenReturn("WOOHOO".getBytes());

		HostedGraphiteReporter reporter = new HostedGraphiteReporter("RosesAreRed", "http://www.hostedgraphite.com", fetcher);
		Counter c = Metrics.newCounter(HostedGraphiteReporterTest.class, "CounterErrorTest");
		c.inc();

		reporter.reportCounterView(new MetricName("test", "counter", "testCounterError"), c, 1000000l);

		verify(fetcher).fetch(reqCapture.capture());
		String captured = new String(reqCapture.getValue().getPayload());
		Assert.assertTrue(captured.startsWith("null.test.counter.testCounterError.count 1 1000000"));

	}

	@Test(groups = "automated")
	public void testGauge() throws Exception {
		URLFetchService fetcher = mock(URLFetchService.class);
		HTTPResponse resp = mock(HTTPResponse.class);
		ArgumentCaptor<HTTPRequest> reqCapture = ArgumentCaptor.forClass(HTTPRequest.class);

		when(fetcher.fetch(any(HTTPRequest.class))).thenReturn(resp);
		when(resp.getResponseCode()).thenReturn(400);
		when(resp.getContent()).thenReturn("WOOHOO".getBytes());

		HostedGraphiteReporter reporter = new HostedGraphiteReporter("RosesAreRed", "http://www.hostedgraphite.com", fetcher);
		Gauge<Double> g = Metrics.newGauge(HostedGraphiteReporterTest.class, "GaugeTest", new Gauge<Double>() {

			@Override
			public Double getValue() {
				return 500.0;
			}
		});

		reporter.reportGaugeView(new MetricName("test", "gauge", "testGauge"), g, 1000000l);

		verify(fetcher).fetch(reqCapture.capture());
		String captured = new String(reqCapture.getValue().getPayload());
		Assert.assertTrue(captured.startsWith("null.test.gauge.testGauge.value 500.0 1000000"));

	}

	@Test(groups = "automated")
	public void testHistogram() throws Exception {
		URLFetchService fetcher = mock(URLFetchService.class);
		HTTPResponse resp = mock(HTTPResponse.class);
		ArgumentCaptor<HTTPRequest> reqCapture = ArgumentCaptor.forClass(HTTPRequest.class);

		when(fetcher.fetch(any(HTTPRequest.class))).thenReturn(resp);
		when(resp.getResponseCode()).thenReturn(202);
		when(resp.getContent()).thenReturn("WOOHOO".getBytes());


		HostedGraphiteReporter reporter = new HostedGraphiteReporter("RosesAreRed", "http://www.hostedgraphite.com", fetcher);
		Histogram hist = Metrics.newHistogram(HostedGraphiteReporterTest.class, "HistogramTest");
		hist.update(1);
		hist.update(2);
		hist.update(3);
		hist.update(4);
		hist.update(5);

		reporter.reportHistogramView(new MetricName("test", "histogram", "testHistogram"), hist, 1000000l);

		verify(fetcher).fetch(reqCapture.capture());
		String captured = new String(reqCapture.getValue().getPayload());
		Assert.assertTrue(captured.contains(".test.histogram.testHistogram.count 5 1000000"));
		Assert.assertTrue(captured.contains(".test.histogram.testHistogram.mean 3.0 1000000"));
		Assert.assertTrue(captured.contains(".test.histogram.testHistogram.median 3.0 1000000"));
		Assert.assertTrue(captured.contains(".test.histogram.testHistogram.98percentile 5.0 1000000"));
		Assert.assertTrue(captured.contains(".test.histogram.testHistogram.99percentile 5.0 1000000"));

	}

	@Test(groups = "automated")
	public void testMeter() throws Exception {
		URLFetchService fetcher = mock(URLFetchService.class);
		HTTPResponse resp = mock(HTTPResponse.class);
		ArgumentCaptor<HTTPRequest> reqCapture = ArgumentCaptor.forClass(HTTPRequest.class);


		when(fetcher.fetch(any(HTTPRequest.class))).thenReturn(resp);
		when(resp.getResponseCode()).thenReturn(202);
		when(resp.getContent()).thenReturn("WOOHOO".getBytes());

		HostedGraphiteReporter reporter = new HostedGraphiteReporter("RosesAreRed", "http://www.hostedgraphite.com", fetcher);
		Meter meter = Metrics.newMeter(HostedGraphiteReporterTest.class, "testMeter", "meteredevent", TimeUnit.SECONDS);
		meter.mark();
		meter.mark();
		meter.mark();
		meter.mark();
		meter.mark();

		reporter.reportMeterView(new MetricName("test", "meter", "testMeter"), meter, 1000000l);

		verify(fetcher).fetch(reqCapture.capture());
		String captured = new String(reqCapture.getValue().getPayload());
		Assert.assertTrue(captured.contains(".test.meter.testMeter.count 5 1000000"));
		Assert.assertTrue(captured.contains(".test.meter.testMeter.mean"));
		Assert.assertTrue(captured.contains(".test.meter.testMeter.oneMinuteRate"));
		Assert.assertTrue(captured.contains(".test.meter.testMeter.fiveMinuteRate"));
		Assert.assertTrue(captured.contains(".test.meter.testMeter.fifteenMinuteRate"));

	}

	@Test(groups = "automated")
	public void testTimer() throws Exception {
		URLFetchService fetcher = mock(URLFetchService.class);
		HTTPResponse resp = mock(HTTPResponse.class);
		ArgumentCaptor<HTTPRequest> reqCapture = ArgumentCaptor.forClass(HTTPRequest.class);

		when(fetcher.fetch(any(HTTPRequest.class))).thenReturn(resp);
		when(resp.getResponseCode()).thenReturn(202);
		when(resp.getContent()).thenReturn("WOOHOO".getBytes());

		HostedGraphiteReporter reporter = new HostedGraphiteReporter("RosesAreRed", "http://www.hostedgraphite.com", fetcher);
		Timer timer = Metrics.newTimer(this.getClass(), "testTimer");
		timer.update(100, TimeUnit.SECONDS);
		timer.update(200, TimeUnit.SECONDS);
		timer.update(300, TimeUnit.SECONDS);
		timer.update(400, TimeUnit.SECONDS);
		timer.update(500, TimeUnit.SECONDS);

		reporter.reportTimerView(new MetricName(this.getClass(), "testTimer", "scope"), timer, 1000000l);

		verify(fetcher).fetch(reqCapture.capture());
		String captured = new String(reqCapture.getValue().getPayload());
		Assert.assertTrue(captured.contains(".com.techtraits.metrics.HostedGraphiteReporterTest.scope.testTimer.count 5 1000000"));
		Assert.assertTrue(captured.contains(".com.techtraits.metrics.HostedGraphiteReporterTest.scope.testTimer.mean 300000.0 1000000"));
		Assert.assertTrue(captured.contains(".com.techtraits.metrics.HostedGraphiteReporterTest.scope.testTimer.median 300000.0 1000000"));
		Assert.assertTrue(captured.contains(".com.techtraits.metrics.HostedGraphiteReporterTest.scope.testTimer.98percentile 500000.0 1000000"));
		Assert.assertTrue(captured.contains(".com.techtraits.metrics.HostedGraphiteReporterTest.scope.testTimer.99percentile 500000.0 1000000"));

	}

	@Test(groups = "automated")
	public void testReportMetrics() throws Exception {
		URLFetchService fetcher = mock(URLFetchService.class);
		HTTPResponse resp = mock(HTTPResponse.class);
		ArgumentCaptor<HTTPRequest> reqCapture = ArgumentCaptor.forClass(HTTPRequest.class);

		when(fetcher.fetch(any(HTTPRequest.class))).thenReturn(resp);
		when(resp.getResponseCode()).thenReturn(202);
		when(resp.getContent()).thenReturn("WOOHOO".getBytes());

		HostedGraphiteReporter reporter = new HostedGraphiteReporter("RosesAreRed", "http://www.hostedgraphite.com", fetcher);


		// Create metrics

		// counter
		Counter c = Metrics.newCounter(this.getClass(), "testReportMetricsCounter");
		c.inc();
		MetricName counterName = new MetricName("test", "counter", "testReportMetricsCounter");

		// timer
		Timer timer = Metrics.newTimer(this.getClass(), "testReportMetricsTimer");
		timer.update(100, TimeUnit.SECONDS);
		timer.update(200, TimeUnit.SECONDS);
		timer.update(300, TimeUnit.SECONDS);
		timer.update(400, TimeUnit.SECONDS);
		timer.update(500, TimeUnit.SECONDS);

		MetricName timerName = new MetricName(this.getClass(), "testReportMetricsTimer", "scope");

		// meter
		Meter meter = Metrics.newMeter(this.getClass(), "testReportMetricsMeter", "meteredevent", TimeUnit.SECONDS);
		meter.mark();
		meter.mark();
		meter.mark();
		meter.mark();
		meter.mark();
		MetricName meterName = new MetricName("test", "meter", "testReportMetricsMeter");

		// histogram
		Histogram hist = Metrics.newHistogram(this.getClass(), "testReportMetricsHistogram");
		hist.update(1);
		hist.update(2);
		hist.update(3);
		hist.update(4);
		hist.update(5);
		MetricName histogramName = new MetricName("test", "histogram", "testReportMetricsHistogram");

		// gauge
		Gauge<Double> g = Metrics.newGauge(this.getClass(), "testReportMetricsGauge", new Gauge<Double>() {

			@Override
			public Double getValue() {
				return 500.0;
			}
		});
		MetricName gaugeName = new MetricName("test", "gauge", "testReportMetricsGauge");

		// add metrics
		Map<MetricName, Metric> metrics = new HashMap<MetricName, Metric>();
		metrics.put(counterName, c);
		metrics.put(timerName, timer);
		metrics.put(meterName, meter);
		metrics.put(histogramName, hist);
		metrics.put(gaugeName, g);


		// report metrics
		reporter.reportMetrics(metrics, 1000000l);

		verify(fetcher).fetch(reqCapture.capture());
		String captured = new String(reqCapture.getValue().getPayload());
		Assert.assertTrue(captured.contains(".com.techtraits.metrics.HostedGraphiteReporterTest.scope.testReportMetricsTimer.count 5 1000000"));
		Assert.assertTrue(captured.contains(".com.techtraits.metrics.HostedGraphiteReporterTest.scope.testReportMetricsTimer.mean 300000.0 1000000"));
		Assert.assertTrue(captured.contains(".com.techtraits.metrics.HostedGraphiteReporterTest.scope.testReportMetricsTimer.median 300000.0 1000000"));
		Assert.assertTrue(captured.contains(".com.techtraits.metrics.HostedGraphiteReporterTest.scope.testReportMetricsTimer.98percentile 500000.0 1000000"));
		Assert.assertTrue(captured.contains(".com.techtraits.metrics.HostedGraphiteReporterTest.scope.testReportMetricsTimer.99percentile 500000.0 1000000"));
		Assert.assertTrue(captured.contains(".test.meter.testReportMetricsMeter.count 5 1000000"));
		Assert.assertTrue(captured.contains(".test.meter.testReportMetricsMeter.mean"));
		Assert.assertTrue(captured.contains(".test.meter.testReportMetricsMeter.oneMinuteRate"));
		Assert.assertTrue(captured.contains(".test.meter.testReportMetricsMeter.fiveMinuteRate"));
		Assert.assertTrue(captured.contains(".test.meter.testReportMetricsMeter.fifteenMinuteRate"));
		Assert.assertTrue(captured.contains(".test.histogram.testReportMetricsHistogram.count 5 1000000"));
		Assert.assertTrue(captured.contains(".test.histogram.testReportMetricsHistogram.mean 3.0 1000000"));
		Assert.assertTrue(captured.contains(".test.histogram.testReportMetricsHistogram.median 3.0 1000000"));
		Assert.assertTrue(captured.contains(".test.histogram.testReportMetricsHistogram.98percentile 5.0 1000000"));
		Assert.assertTrue(captured.contains(".test.histogram.testReportMetricsHistogram.99percentile 5.0 1000000"));
		Assert.assertTrue(captured.contains(".test.gauge.testReportMetricsGauge.value 500.0 1000000"));
		Assert.assertTrue(captured.contains(".test.counter.testReportMetricsCounter.count 1 1000000"));

	}

}
