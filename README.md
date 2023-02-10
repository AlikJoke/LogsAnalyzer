# LogsAnalyzer (imperative version)
Analyzer for log files based on Elastic and Spring Boot. The analyzer is able to process both files and archives with log files with different patterns of log records. 
Log files are loaded via the REST API, after which the logs are parsed and indexed in the external configured Elastic storage. After indexing, it is possible to perform various filtering / aggregating queries to these log records.
Application is able to collect statistics by logs.
This version of application (imperative) provide support for standalone mode of working (with embedded MongoDB, embedded Solr for standalone mode as alternative for ElasticSearch Server and in-memory cache / events as alternative for Redis Server).
Moreover, this version provide extended functionality for analyzing HAR logs and (optional if present) linked log files.
