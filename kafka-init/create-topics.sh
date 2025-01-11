#!/bin/bash

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
cub kafka-ready -b kafka:9092 1 20 || exit 1

TOPIC_NAME="user-events"

# Check if the topic already exists
if kafka-topics --list --bootstrap-server kafka:9092 | grep -q "^${TOPIC_NAME}$"; then
  echo "Topic '${TOPIC_NAME}' already exists."
  kafka-topics --delete --topic ${TOPIC_NAME} --bootstrap-server kafka:9092
fi

echo "Creating topic '${TOPIC_NAME}'..."
kafka-topics --create \
  --topic ${TOPIC_NAME} \
  --bootstrap-server kafka:9092 \
  --replication-factor 1 \
  --partitions 1
echo "Topic '${TOPIC_NAME}' created successfully!"

