REFERENCES:
https://railsadventures.wordpress.com/2019/12/02/from-graphite-to-prometheus-things-ive-learned/

# 2023-12-14 #9 Meet Prometheus
Prometheus has been created in 2012 by [SoundCloud](https://developers.soundcloud.com/blog/prometheus-monitoring-at-soundcloud) as a "pet project" and becoming popular to "slice and dice" metrics accross multi-dimensional metrics.

Like for [Graphite](../2023-12-07_Meet_Graphite/README.md) did with [RRDtool](https://oss.oetiker.ch/rrdtool/) + [Cacti](https://www.cacti.net/), Why did SoudCloud create [Prometheus](https://prometheus.io/)? 

Why SoudCloud decided to move away from Graphite ? 

What are the new trade-offs ?

Does Prometheus can cover all [Graphite](../2023-12-07_Meet_Graphite/README.md) use cases ?

## Why Prometheus ?
Reference: 
- https://developers.soundcloud.com/blog/prometheus-monitoring-at-soundcloud
- [Graphite queries](../2023-12-07_Meet_Graphite/README.md#query-language)

When the software becomes complex, telemetry can impact the metrics backend due to its limitations. When those limitations become important, new trade-offs and tools are created but do not cover all the previous use cases. 

Complex software requires a metric backend which supports high dimension cardinality and as mentioned inside SoundCloud's [initial blog post](https://developers.soundcloud.com/blog/prometheus-monitoring-at-soundcloud) multi-dimensional queries is really important to understand the performance or error issues.

While [Graphite](../2023-12-07_Meet_Graphite/README.md) can support long storage for monotonic counters metrics with true [monotonicity](../2023-12-07_Meet_Graphite/README.md#telemetry-temporality) and is suitable to monitor business and tech metrics with few dimensions, [Prometheus](https://prometheus.io) can support high cardinality metrics and queries easier.

Prometheus is really easy when adding dimensions on existing metrics without breaking all the queries since dimensions are multi-dimensional as opposed to [Graphite queries](../2023-12-07_Meet_Graphite/README.md#query-language).

Prometheus is less impacted than Graphite to support aggregation over a or more dimensions in terms of performance (ie: rate of all dimensions of a metric).

## What is Prometheus ?
Prometheus has been created for monitoring and alerting only. It is the second project [incubated by the CNCF in 2016 after Kubernetes](https://prometheus.io/docs/introduction/overview/) and has been popular since [it has been used by Docker](https://developers.soundcloud.com/blog/prometheus-monitoring-at-soundcloud).

The data model combined with PromQL solve the problem of querying multi-dimensional data.

Prometheus is focused on reliability at edge and uses a HTTP pull model to avoid loosing telemetry during an outage.
[A previous post about monotonicity trade-offs illustrate the problem during an outage](../2023-11-09_Monotonicity/README.md#cumulative-vs-delta)

It can also support [pushing time series](https://prometheus.io/docs/instrumenting/pushing/) for short lived time jobs like batch on function as a service but with [limited use case](https://prometheus.io/docs/practices/pushing/).

Prometheus has not been designed to support centralized storage and uses remote write to scale. 

## Quickstart

A docker compose file is available: [compose.yml](./compose.yml)

```yaml
services:
  prometheus:
    image: prom/prometheus:v2.47.2
    ports:
      - 9090:9090
```

```bash
docker compose up
```

View the `up` metric which is automatically added by prometheus to [monitor](http://localhost:9090/graph?g0.expr=up&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h):
- http://localhost:9090/graph?g0.expr=up&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h

An example of prometheus configuration is also available to scrape itself: [prometheus.yml](./prometheus/prometheus.yml)
```yaml
global:
  scrape_interval: 15s # By default, scrape targets every 15 seconds.

  external_labels:
    monitor: 'prometheus-monitor'

scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: 'prometheus'

    # Override the global default and scrape targets from this job every 5 seconds.
    scrape_interval: 5s

    static_configs:
      - targets: ['localhost:9090']
```

A demo is also available to compare Prometheus with other backends in a [previous post demo](../2023-11-09_Monotonicity/demo/README.md#context)

## Architecture
Reference: https://prometheus.io/docs/introduction/overview/#architecture

The prometheus server components 

Service discovery, ...

## Exporters

## Backends
Cortex: https://grafana.com/blog/2020/08/20/cortex-the-scalable-prometheus-project-has-advanced-to-incubation-within-cncf/, thanos, ...

Mimir

## Query Language
PromQL
https://grafana.com/blog/2021/05/13/how-to-correlate-graphite-metrics-and-loki-logs/

https://promlabs.com/promql-cheat-sheet/

## AlertManager

### PushGateway
https://github.com/prometheus/pushgateway

https://prometheus.io/docs/practices/pushing/
https://prometheus.io/docs/instrumenting/pushing/

## Graphing

## Additional tools

### Grafana

### Consul

## Backends comparison
[Graphite vs VictoriaMetrics vs Prometheus vs Mimir demo from previous post](../2023-11-09_Monotonicity/demo/README.md#datapoints-visualization-comparison)

## Conclusion