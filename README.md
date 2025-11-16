# 🕵️ Real-Time Fraud Detection Pipeline (Scala + Kafka)

## 📌 Overview
This project implements a **real-time fraud detection system** using:

- **Apache Kafka** for ingestion and streaming
- **Scala** for producer + consumer
- **Custom Rule Engine** for fraud detection
- **Fraud Alerts** published to a separate Kafka topic

The system reads transaction data from CSV, publishes them to Kafka, evaluates rules, and produces fraud alerts.

---Steps to Run the Project---

	1. Open terminal
	2. Start docker app
	cd fraud-detection/docker
	docker-compose up -d
	3. docker ps
	4. docker exec -it kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
	5. IN CASE TOPIC IS NOT CREATED 
	6. docker exec -it docker_kafka_1 kafka-topics.sh --create --topic transactions --bootstrap-server localhost:9092
    7. docker exec -it docker_kafka_1 kafka-topics.sh --create --topic fraud_alerts --bootstrap-server localhost:9092
    8. Open new terminal
    9. cd fraud-detection/scala
    10. sbt run
      a. sbt "runMain producer.FraudDetectionProducer"
      b. sbt "runMain consumer.FraudDetectionConsumer"
    11. Check the output in terminal

| Rule                                                        | Description                                                 |
| ----------------------------------------------------------- | ----------------------------------------------------------- |
| **1. High Velocity (More than 5 transactions in 1 minute)** | Detects rapid-fire spending behavior.                       |
| **2. Amount Spike Detection**                               | Flags if current transaction amount > 2× previous average.  |
| **3. New Device Detected**                                  | Alerts if the customer suddenly uses a new device type.     |
| **4. Location Change / Impossible Travel**                  | Detects if transaction comes from a new location suddenly.  |
| **5. High-Risk Merchant Category**                          | Alerts for dangerous spend categories like crypto/gambling. |

🛠 Tech Stack:
   Scala 2.13
   Kafka Producer/Consumer API
   Circe (JSON serialization)
   OpenCSV (CSV reader)

