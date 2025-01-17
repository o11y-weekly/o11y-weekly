# dotnet debug

## dotnet monitor
### install
```bash
dotnet tool install -g dotnet-monitor
```

### configure CORS
Reference: https://github.com/dotnet/diagnostics/pull/1377
```bash
$env:DotnetMonitor_CorsConfiguration__AllowedOrigins='https://dotnet-monitor-ui.dev'
```

### start
```bash
dotnet monitor collect -u http://0.0.0.0::52323 --metricUrls http://0.0.0.0:52325 --no-auth
```

## dotnet trace

### install
```bash
dotnet tool install -g dotnet-trace
```

### collect
```bash
dotnet trace collect -p1
```

### report
```bash
dotnet trace report Process1_20240226_091340.nettrace topN -n100 --inclusive
```

```bash
dotnet trace report Process1_20240226_091340.nettrace topN -n100
```

### visualize
References:
 - [Flame graphs and more by Hanselman](https://www.hanselman.com/blog/dotnettrace-for-net-core-tracing-in-perfview-speedscope-chromium-event-trace-profiling-flame-graphs-and-more)

#### Perfview
Reference: [perfview](https://github.com/microsoft/perfview)
View flame graph by opening the nettrace or collect directly the process by PID

#### Chromium
- The report can be converted
```bash
dotnet trace convert dotnet.exe_20240925_172930.nettrace --format chromium
```
- Or simply directly collected with the chromium format:
```bash
dotnet trace collect --format chromium
```
- Open chrome [chrome://tracing](chrome://tracing)

#### Speedscope
- The report can be converted
```bash
dotnet trace convert dotnet.exe_20240925_172930.nettrace --format speedscope
```
- Or simply directly collected with the chromium format:
```bash
dotnet trace collect --format speedscope
```
- Open Speedscope from any browser [Speedscope.app](https://www.speedscope.app/)

## dotnet dump
### install
```bash
dotnet tool install -g dotnet-dump
```

### collect
```bash
dotnet dump collect -p1
```

### analyze
```bash
dotnet dump analyze <FilePath>
```

```bash
dotnet dump analyze core_20240229_101513 -c 'dumpheap -stat' -c exit | more
```

#### heapstat
```bash
dumpheap -stat
``` 
