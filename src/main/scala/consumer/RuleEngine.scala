package consumer

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object RuleEngine {
  def applyRules(df: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    df.withColumn("isHighValue", $"amount" > 10000)
      .withColumn("isForeign", $"location" =!= "IN")
      .withColumn(
        "alert",
        when($"isHighValue" || $"isForeign", lit("⚠️ Potential Fraud"))
          .otherwise(lit("✅ Normal"))
      )
  }
}
