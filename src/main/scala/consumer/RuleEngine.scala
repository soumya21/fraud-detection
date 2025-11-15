package consumer

import common.Transaction
import scala.collection.mutable
import java.time.Instant

case class TransactionWithFlags(
                                 txn: Transaction,
                                 rule1: Boolean,
                                 rule2: Boolean,
                                 rule3: Boolean,
                                 rule4: Boolean,
                                 rule5: Boolean,
                                 isFraud: Boolean
                               )

object RuleEngine {

  // In-memory states
  val txnHistory: mutable.Map[String, List[Transaction]] = mutable.Map.empty // userId -> list of recent txns
  val avgDailySpend: Double = 2000.0
  val maxLifetimeSpend: Double = 15000.0
  val userBillingCountry: Map[String, String] = Map.empty.withDefaultValue("US")
  val countryCoords: Map[String, (Double, Double)] = Map(
    "US" -> (37.0902, -95.7129),
    "IN" -> (20.5937, 78.9629),
    "UK" -> (55.3781, -3.4360),
    "CN" -> (35.8617, 104.1954)
  )

  // ---------------- Haversine distance ----------------
  def haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double = {
    val R = 6371 // km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.pow(Math.sin(dLat/2), 2) + Math.cos(Math.toRadians(lat1)) *
      Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon/2),2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
    R * c
  }

  // ---------------- Apply all rules ----------------
  def applyRules(txn: Transaction): TransactionWithFlags = {
    println(txn)
    val userTxns = txnHistory.getOrElse(txn.userId, List.empty)

    // ---- Rule 1: >5 txns in last 10 minutes ----
    val tenMinAgo = txn.timestamp - 10*60*1000
    val recentTxns = (txn :: userTxns).filter(_.timestamp >= tenMinAgo)
    val rule1 = recentTxns.size > 5

    // ---- Rule 2: total spend last 1h > 3x avg ----
    val oneHourAgo = txn.timestamp - 3600*1000
    val spendLastHour = recentTxns.filter(_.timestamp >= oneHourAgo).map(_.amount).sum
    val rule2 = spendLastHour > avgDailySpend * 3

    // ---- Rule 3: impossible travel ----
    val rule3 = if (recentTxns.nonEmpty) {
      val lastTxn = recentTxns.head
      val (lat1, lon1) = countryCoords.getOrElse(lastTxn.location, (0.0, 0.0))
      val (lat2, lon2) = countryCoords.getOrElse(txn.location, (0.0, 0.0))
      val dist = haversine(lat1, lon1, lat2, lon2)
      val timeDeltaH = (txn.timestamp - lastTxn.timestamp)/3600000.0
      timeDeltaH > 0 && dist/timeDeltaH > 1000 // km/h
    } else false

    // ---- Rule 4: country mismatch ----
    val billingCountry = userBillingCountry(txn.userId)
    val rule4 = txn.location != billingCountry

    // ---- Rule 5: very high transaction ----
    val rule5 = txn.amount > 10000.0 || txn.amount > maxLifetimeSpend

    // ---- Update in-memory state ----
    txnHistory.update(txn.userId, recentTxns)

    val isFraud = rule1 || rule2 || rule3 || rule4 || rule5
    TransactionWithFlags(txn, rule1, rule2, rule3, rule4, rule5, isFraud)
  }
}
