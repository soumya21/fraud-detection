package consumer

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

object FraudDetection {

  def main(args: Array[String]): Unit = {

    // Initialize Spark session
    implicit val spark: SparkSession = SparkSession.builder()
      .appName("FraudDetection")
      .master("local[*]")
      .config("spark.driver.host", "127.0.0.1")
      .config("spark.sql.streaming.forceDeleteTempCheckpointLocation", "true")
      // ⚠️ Add these two lines to global config to ensure consistency
      .config("spark.kafka.bootstrap.servers", "localhost:9092")
      .config("spark.kafka.security.protocol", "PLAINTEXT")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // The lines below are redundant if set above in .config(), but harmless if duplicated.
    // spark.conf.set("spark.kafka.bootstrap.servers", "localhost:9092")
    // spark.conf.set("spark.kafka.security.protocol", "PLAINTEXT")
    // spark.conf.set("spark.kafka.sasl.mechanism", "PLAIN") // This line might be triggering the issue, remove it.

    // Schema for JSON messages
    val transactionSchema = StructType(Seq(
      StructField("transactionId", StringType),
      StructField("userId", StringType),
      StructField("amount", DoubleType),
      StructField("location", StringType),
      StructField("timestamp", LongType)
    ))

    // Read transactions from Kafka (plaintext mode)
    val kafkaStream: DataFrame = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("kafka.security.protocol", "PLAINTEXT")
      // 👇 ADD THESE LINES 👇
      .option("kafka.sasl.mechanism", "PLAIN") // Keep this if your Kafka broker explicitly uses PLAIN, but often removing it helps.
      .option("kafka.sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"\" password=\"\";")
      // 👆 ADD THESE LINES 👆
      .option("subscribe", "transactions")
      .option("startingOffsets", "latest")
      .load()

    // ... (rest of your code remains the same) ...
    // Extract JSON and parse to DataFrame
    val transactions = kafkaStream
      .selectExpr("CAST(value AS STRING) as json")
      .select(from_json(col("json"), transactionSchema).as("data"))
      .select("data.*")

    // Apply fraud detection rules
    // Make sure 'RuleEngine.applyRules' is defined elsewhere in your project
    val processed = RuleEngine.applyRules(transactions)

    // Write processed stream to console for now
    val query = processed.writeStream
      .outputMode("append")
      .format("console")
      .option("truncate", false)
      .start()

    query.awaitTermination()
  }
}
