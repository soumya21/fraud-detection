package consumer

import org.apache.spark.sql.{DataFrame, ForeachWriter, Row}
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model._

object AlertSink {
  class FraudAlertWriter extends ForeachWriter[Row] {
    val snsClient: SnsClient = SnsClient.builder().build()
    val topicArn: String = "arn:aws:sns:ap-south-1:123456789012:fraud-alerts"

    def open(partitionId: Long, epochId: Long): Boolean = true
    def process(row: Row): Unit = {
      val msg = s"⚠️ Fraud detected! TxnID: ${row.getAs[String]("transactionId")}, Amount: ${row.getAs[Double]("amount")}"
      val request = PublishRequest.builder().topicArn(topicArn).message(msg).build()
      snsClient.publish(request)
      println(s"Alert sent: $msg")
    }
    def close(errorOrNull: Throwable): Unit = {}
  }
}
