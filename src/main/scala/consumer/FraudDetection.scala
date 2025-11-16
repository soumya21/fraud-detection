package consumer

import org.apache.kafka.clients.consumer.{KafkaConsumer, ConsumerRecords}
import java.util.{Properties, Collections}
import scala.collection.JavaConverters._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import schema.Transaction

object FraudDetection extends App {

  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("group.id", "fraud-consumer-group")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("auto.offset.reset", "earliest")

  val consumer = new KafkaConsumer[String, String](props)
  consumer.subscribe(Collections.singletonList("transactions"))

  val mapper = new ObjectMapper().registerModule(DefaultScalaModule)

  println("🚀 TransactionConsumer started...")

  while(true) {
    val records: ConsumerRecords[String, String] = consumer.poll(java.time.Duration.ofSeconds(1))
    for(record <- records.asScala) {
      try {
        val txn = mapper.readValue(record.value(), classOf[Transaction])
        val result = RuleEngine.applyRules(txn)
        println(s"Processed: ${result.txn.transactionId}, Fraud: ${result.isFraud}, " +
          s"R1:${result.rule1} R2:${result.rule2} R3:${result.rule3} R4:${result.rule4} R5:${result.rule5}")
      } catch {
        case e: Exception => println(s"❌ Failed to process record: ${e.getMessage}")
      }
    }
  }
}
