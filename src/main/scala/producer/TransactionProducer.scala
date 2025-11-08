package producer

import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.StringSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import common.Transaction
import scala.util.Random

object TransactionProducer extends App {

  val props = new java.util.Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
  props.put(ProducerConfig.ACKS_CONFIG, "all")
  props.put(ProducerConfig.LINGER_MS_CONFIG, "10")
  props.put(ProducerConfig.RETRIES_CONFIG, "3")

  val producer = new KafkaProducer[String, String](props)
  val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
  val topic = "transactions"

  sys.addShutdownHook {
    println("🛑 Shutting down producer...")
    producer.flush()
    producer.close()
  }

  println("🚀 TransactionProducer started ...")

  while (true) {
    val txn = Transaction(
      java.util.UUID.randomUUID().toString,
      "user-" + Random.nextInt(100),
      10.0 + (Random.nextDouble() * (20000.0 - 10.0)),
      Seq("IN", "US", "UK", "CN").apply(Random.nextInt(4)),
      System.currentTimeMillis()
    )

    val json = mapper.writeValueAsString(txn)
    val record = new ProducerRecord[String, String](topic, txn.userId, json)

    producer.send(record, new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
        if (exception != null)
          println(s"❌ Error producing message: ${exception.getMessage}")
        else
          println(s"✅ Produced: $json")
      }
    })

    Thread.sleep(1000)
  }
}
