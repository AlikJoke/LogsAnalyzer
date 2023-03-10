# LogsAnalyzer (imperative version)
Analyzer for log files based on Elastic and Spring Boot. The analyzer is able to process both files and archives with log files with different patterns of log records. 
Log files are loaded via the REST API, after which the logs are parsed and indexed in the external configured Elastic storage. After indexing, it is possible to perform various filtering / aggregating queries to these log records.
Application is able to collect statistics by logs.
This version of application (imperative) provide support for standalone mode of working (with embedded MongoDB, Lucene for standalone mode as alternative for ElasticSearch Server and in-memory cache / events as alternative for Redis Server).
Moreover, this version provide extended functionality for analyzing HAR logs and (optional if present) linked log files.
Supports working with the application through a telegram bot (https://t.me/LogsAnalyzerBot).
Application REST API is available through the Open API in the swagger interface and through the commands of a telegram bot.

# Анализатор логов (расширенная версия)
Приложение предназначено для работы с логами (протоколирование приложение) и HAR-логов взаимодействия с сервером.
В части работы со стандартными логами приложение предоставляет следующие функции:
1. Индексация данных в хранилище
2. Поиск / агрегация по проиндексированным данным
3. Формирование статистики по проиндексированным логам
4. Фоновая индексация логов (с загрузкой их по сети по настройкам)
5. Удаление не нужных логов

В части работы с HAR-логами имеется следующий функционал:
1. Сохранение HAR в хранилище
2. Удаление HAR
3. Фильтрация / сортировка HAR по различным критериям
4. Анализ HAR на предмет наиболее важных характеристик

Приложение работает в контексте пользователя, т.е. для доступа к функционалу требуется регистрация учетной записи в системе. Пользователь имеет возможность задать настройки оповещений (через e-mail / telegram) и фоновой индексации данных (скачивание логов по сети по настройкам). Поисковые запросы пользователя сохраняются в виде истории (пользователь имеет возможность очищать записи из этой истории при необходимости).
Часть функционала доступна через Telegram-бота (https://t.me/LogsAnalyzerBot).

Поиск / агрегация по логам осуществляются путем указания тела запроса:
1. В режиме "из коробки" формат следующий: https://lucene.apache.org/core/2_9_4/queryparsersyntax.html.
2. В распределенном режиме: либо "простые" запросы (см. https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html), либо в расширенном виде в формате JSON-запроса к Elasticsearch.

# Техническая реализация
Приложение может запускаться в двух режимах:
1. "Из коробки" - все, что требуется для работы, идет с самим jar приложения
2. Распределенный режим с внешним ПО - различные хранилища и требуемые сервисы развернуты отдельно

Режим "из коробки" работает на основе таких технологий, как:
1. Apache Lucene: для работы с логами
2. Embedded MongoDB: для работы с пользовательскиим данными, HAR и статистикой
3. В качестве шины событий используется инфраструктура для Spring-событий

По сути данный режим удобен для индивидуального использования.

Распределенный режим работает с учетом возможности масштабирования приложения (развертывание нескольких серверов). Технологический стек следующий:
1. ElasticSearch: для работы с логами
2. MongoDB: для работы с пользовательскиим данными, HAR и статистикой
3. Redis: как система кэширования и шина событий (Redis Events), а также для работы с HTTP-сессиями пользователей

Приложение работает через REST API (документация доступна через Swagger UI - интеграция с Open API). API поддерживает интернационализацию. Для работы с REST API требуется Basic-аутентификация (далее используется механизм сессий через Spring-куку SESSION).

Для технического управления приложением используется Spring Actuator и соответствующие endpoint.
