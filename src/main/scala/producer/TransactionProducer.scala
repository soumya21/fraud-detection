package producer

import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.StringSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import schema.Transaction
import scala.util.Random


object TransactionProducer extends App {

  val props = new java.util.Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])

  val producer = new KafkaProducer[String, String](props)
  val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
  val topic = "transactions"

  val locations = Seq("US", "IN", "UK", "CN")

  while (true) {
    val txn = Transaction(
      java.util.UUID.randomUUID().toString,// card
      "user-" + Random.nextInt(10), // smaller user pool to trigger Rule1
      10.0 + Random.nextDouble() * 20000.0, // some large values to trigger Rule5
      locations(Random.nextInt(locations.size)), // random location
      System.currentTimeMillis()//event time
    )

    val record = new ProducerRecord[String, String](topic, txn.userId, mapper.writeValueAsString(txn))

    try {
      producer.send(record)
      println(s"Produced: $txn")
    } catch {
      case e: Exception => println(s"❌ Error producing message: ${e.getMessage}")
    }

    Thread.sleep(500) // produce 2 messages per second for faster testing
  }
}
