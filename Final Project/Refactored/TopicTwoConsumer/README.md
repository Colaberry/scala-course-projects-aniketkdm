# MicroService 3

## Objective:
Reads messages from topic 2 (messages written by KafkaConsumer) and writes them to elastic search.
The received messages are converted to JsonString before writting into elastic search.

## Build and Run
sbt run
