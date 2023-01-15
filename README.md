# LogsParser
Parser for log files based on Elastic and Spring Boot. The parser is able to process both files and archives with log files with different patterns of log records. 
Log files are loaded via the REST API, after which the logs are parsed and indexed in the external configured Elastic storage. After indexing, it is possible to perform various filtering / aggregating queries to these log records.
At the end of the application, all indexed data for the session is cleared automatically (or manually through REST API).
