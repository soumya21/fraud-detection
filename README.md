# 🕵️ Fraud Detection Pipeline

## Overview
This is an end-to-end **near real-time fraud detection system** built with:
- **Kafka** for event ingestion
- **Spark Structured Streaming** for detection
- **AWS SNS** for alerts
- **Airflow** for orchestration

## Project Structure
See `/src/main/scala` for:
- `producer`: Kafka transaction simulator
- `consumer`: Spark streaming fraud detector
- `common`: shared models and schemas

## How to Run
1. Start Kafka:
   ```bash
   cd docker && docker-compose up -d
