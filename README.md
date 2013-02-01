hosted-graphite-reporter
========================

A simple library to report codahale metrics to hosted graphite from google app engine

Building from source
-------------

* Install maven 3
* Configure repos in settings.xml
* git clone git://github.com/usmanismail/hosted-graphite-reporter.git
* cd hosted-graphite-reporter
* mvn clean install

Installing
-------------
* Download [hosted-graphite-reporter-0.0.1.jar](https://github.com/usmanismail/hosted-graphite-reporter/blob/master/releases/hosted-graphite-reporter-0.0.1.jar?raw=true) 
* Add to class path


Usage
-------------

	import com.google.appengine.api.urlfetch.URLFetchService;
	import com.yammer.metrics.core.Meter;
	import com.yammer.metrics.Metrics;
	//...
	
	public class MetricsSample {
	
		public void runMetricsSample(String hostedGraphiteSecret, String hostedGraphiteUrl, URLFetchService fetcher) {
			HostedGraphiteReporter reporter = new HostedGraphiteReporter(hostedGraphiteSecret, hostedGraphiteUrl, fetcher);
			Meter meter = Metrics.newMeter(this.class, "MeterMetric");

			meter.mark();

			//Report one metric
			reportMeterView(new MetricName(this.getClass(), "somename"), meter, System.currentTimeMillis());
	
			//Report lots of metrics
			Map<MetricName, Metric> metrics = new HashMap<MetricName, Metric>();
			metrics.put(new MetricName(this.getClass(), "somename"), meter);
			reportMetrics(metrics, System.currentTimeMillis());
	
			//Report Arbitrary Values
			metricsRecord
			List<MetricRecord> metricsRecords = new List<MetricRecord>();
			metricsRecords.add(new MetricRecord(new MetricName(this.getClass(), "somename"), "postfix", 1.5, System.currentTimeMillis()));
			postMetrics(metricsRecords);
		}
	}