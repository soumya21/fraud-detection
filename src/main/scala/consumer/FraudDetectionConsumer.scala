package consumer



import java.time.Duration
import java.util.{Collections, Properties}
import org.apache.kafka.clients.consumer.KafkaConsumer
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import scala.collection.JavaConverters._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import schema.BankTransaction



object FraudDetectionConsumer {

  def start(): Unit = {
    println("🚀 Fraud Detection Consumer is now running...")

    // ---- CONSUMER PROPS ----
    val consumerProps = new Properties()
    consumerProps.put("bootstrap.servers", "localhost:9092")
    consumerProps.put("group.id", "fraud-detector")
    consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    consumerProps.put("auto.offset.reset", "latest")
    consumerProps.put("enable.auto.commit", "false")
    consumerProps.put("auto.commit.interval.ms", "1000")
    consumerProps.put("max.poll.records", "50")


    val consumer = new KafkaConsumer[String, String](consumerProps)
    consumer.subscribe(java.util.Arrays.asList("transactions"))



    // ---- ALERT PRODUCER ----
    val producerProps = new Properties()
    producerProps.put("bootstrap.servers", "localhost:9092")
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](producerProps)

    while (true) {

      val records = consumer.poll(Duration.ofMillis(1000))
//      println(s"Polled ${records.count()} records")

      for (record <- records.asScala) {
        val parsed = decode[BankTransaction](record.value())
//        println("RAW MESSAGE → " + record.value())


        parsed match {
          case Left(err) =>
            println(s"❌ JSON parse error: $err")

          case Right(txn) =>
            // Run rules
            val alerts = FraudDetectionRule.evaluate(txn)


            alerts.foreach { alert =>
              val json = alert.asJson.noSpaces
              val msg = new ProducerRecord[String, String]("fraud-alerts", alert.customerId, json)
              producer.send(msg)
              println(s"🚨 ALERT → $json")
            }
            consumer.commitSync()
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    start()
  }
}





