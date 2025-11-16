package producer

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import com.opencsv.CSVReader
import java.io.FileReader
import io.circe.syntax._
import io.circe.generic.auto._
import schema.{BankFraudAlert,BankTransaction}

object FraudDetectionProducer {

  def main(args: Array[String]): Unit = {

    val topic = "transactions"

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("auto.offset.reset", "earliest")


    val producer = new KafkaProducer[String, String](props)

    val reader = new CSVReader(new FileReader("./data/Bank_Transaction_Fraud_Detection.csv"))
    val it = reader.iterator()

    it.next() // skip header

    while (it.hasNext) {
      val cols = it.next()

      // Ensure row has all expected 24 fields
      if (cols.length != 24) {
        println(s"⚠ Skipping malformed row: ${cols.mkString(",")}")
      } else {
        try {

          val txn = BankTransaction(
            customerId = cols(0).trim,
            customerName = cols(1).trim,
            gender = cols(2).trim,
            age = cols(3).trim.toInt,
            state = cols(4).trim,
            city = cols(5).trim,
            bankBranch = cols(6).trim,
            accountType = cols(7).trim,
            transactionId = cols(8).trim,
            transactionDate = cols(9).trim,
            transactionTime = cols(10).trim,
            transactionAmount = cols(11).trim.toDouble,
            merchantId = cols(12).trim,
            transactionType = cols(13).trim,
            merchantCategory = cols(14).trim,
            accountBalance = cols(15).trim.toDouble,
            transactionDevice = cols(16).trim,
            transactionLocation = cols(17).trim,
            deviceType = cols(18).trim,
            isFraud = cols(19).trim.toInt,
            transactionCurrency = cols(20).trim,
            customerContact = cols(21).trim,
            transactionDescription = cols(22).trim,
            customerEmail = cols(23).trim
          )

          val json = txn.asJson.noSpaces
          producer.send(new ProducerRecord(topic, txn.customerId, json))
          println(s"Sent → $json")

          Thread.sleep(200)

        } catch {
          case e: Exception =>
            println(s"❌ Skipping row due to error: ${e.getMessage}")
            println(s"Row → ${cols.mkString(",")}")
        }
      }
    }

    reader.close()
    producer.close()
  }
}
