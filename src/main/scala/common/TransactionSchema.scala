package common

import org.apache.spark.sql.types._

object TransactionSchema {
  val schema: StructType = StructType(Seq(
    StructField("transactionId", StringType),
    StructField("userId", StringType),
    StructField("amount", DoubleType),
    StructField("location", StringType),
    StructField("timestamp", LongType) // original Kafka field
  ))
}
