package consumer

import scala.collection.mutable
import java.time.{LocalDateTime, ZoneId, Duration}

// ------- INPUT -------
import schema.{BankTransaction,BankFraudAlert}


object FraudDetectionRule {

  // ================================
  // 📌 STATE STORES (In-Memory)
  // ================================
  private val velocityStore = mutable.Map[String, List[Long]]()             // customerId -> list of timestamps
  private val avgAmountStore = mutable.Map[String, Double]()                // customerId -> avg amount
  private val deviceStore = mutable.Map[String, String]()                   // customerId -> lastDevice
  private val locationStore = mutable.Map[String, String]()                 // customerId -> lastLocation
  private val highRiskMerchantSet = Set("GAMBLING", "CRYPTO", "FRAUDSTORE") // Example categories

  // ================================
  // 🚨 EVALUATE ALL FRAUD RULES
  // ================================
  def evaluate(txn: BankTransaction): List[BankFraudAlert] = {
    println(txn)
    val r1 = highVelocityRule(txn)
    val r2 = amountSpikeRule(txn)
    val r3 = deviceChangeRule(txn)
    val r4 = locationChangeRule(txn)
    val r5 = riskMerchantRule(txn)
    val r6 = highAmountRule(txn)
    val r7 = locationMismatch(txn)
    val r8 = negativeAccountBalance(txn)

    List(r1, r2, r3, r4, r5,r6,r7,r8).flatten
  }

  // ====================flatten==============================
  // RULE 1 — HIGH VELOCITY TRANSACTIONS ( >5 in 1 min )
  // ==================================================
  private def highVelocityRule(txn: BankTransaction): Option[BankFraudAlert] = {
    val now = System.currentTimeMillis()
    val existing = velocityStore.getOrElse(txn.customerId, List())

    // Remove older than 1 minute
    val filtered = existing.filter(ts => now - ts <= 60000)

    // Updated list
    val updated = now :: filtered
    velocityStore.put(txn.customerId, updated)

    if (updated.size > 5)
      Some(
        BankFraudAlert(
          customerId = txn.customerId,
          transactionId = txn.transactionId,
          reason = s"HIGH_VELOCITY: ${updated.size} transactions in 1 minute",
          transactionAmount = txn.transactionAmount,
          merchantCategory = txn.merchantCategory
        )
      )
    else None
  }

  // ==================================================
  // RULE 2 — AMOUNT SPIKE DETECTION ( >2× average )
  // ==================================================
  private def amountSpikeRule(txn: BankTransaction): Option[BankFraudAlert] = {
    val previousAvg = avgAmountStore.getOrElse(txn.customerId, txn.transactionAmount)

    val newAvg = (previousAvg + txn.transactionAmount) / 2
    avgAmountStore.put(txn.customerId, newAvg)

    if (txn.transactionAmount > previousAvg * 2)
      Some(
        BankFraudAlert(
          customerId = txn.customerId,
          transactionId = txn.transactionId,
          reason = s"AMOUNT_SPIKE: Current ${txn.transactionAmount}, Previous Avg $previousAvg",
          transactionAmount = txn.transactionAmount,
          merchantCategory = txn.merchantCategory
        )
      )
    else None
  }

  // ==================================================
  // RULE 3 — NEW DEVICE DETECTED
  // ==================================================
  private def deviceChangeRule(txn: BankTransaction): Option[BankFraudAlert] = {
    val lastDevice = deviceStore.get(txn.customerId)

    if (lastDevice.isEmpty) {
      deviceStore.put(txn.customerId, txn.deviceType)
      return None
    }

    if (lastDevice.get != txn.deviceType) {
      deviceStore.put(txn.customerId, txn.deviceType)

      return Some(
        BankFraudAlert(
          customerId = txn.customerId,
          transactionId = txn.transactionId,
          reason = s"NEW_DEVICE_DETECTED: Prev=${lastDevice.get}, New=${txn.deviceType}",
          transactionAmount = txn.transactionAmount,
          merchantCategory = txn.merchantCategory
        )
      )
    }

    None
  }

  // ==================================================
  // RULE 4 — LOCATION CHANGE (IMPOSSIBLE TRAVEL)
  // ==================================================
  private def locationChangeRule(txn: BankTransaction): Option[BankFraudAlert] = {
    val lastLoc = locationStore.get(txn.customerId)

    if (lastLoc.isEmpty) {
      locationStore.put(txn.customerId, txn.transactionLocation)
      return None
    }
//    println(txn)
//    println(s"${lastLoc.get} ${txn.transactionLocation}")
    if (lastLoc.get != txn.transactionLocation) {
      locationStore.put(txn.customerId, txn.transactionLocation)

      return Some(
        BankFraudAlert(
          customerId = txn.customerId,
          transactionId = txn.transactionId,
          reason = s"LOCATION_CHANGE: Prev=${lastLoc.get}, New=${txn.transactionLocation}",
          transactionAmount = txn.transactionAmount,
          merchantCategory = txn.merchantCategory
        )
      )
    }

    None
  }

  // ==================================================
  // RULE 5 — HIGH RISK MERCHANT CATEGORY
  // ==================================================
  private def riskMerchantRule(txn: BankTransaction): Option[BankFraudAlert] = {
    if (highRiskMerchantSet.contains(txn.merchantCategory.toUpperCase))
      Some(
        BankFraudAlert(
          customerId = txn.customerId,
          transactionId = txn.transactionId,
          reason = s"HIGH_RISK_MERCHANT: ${txn.merchantCategory}",
          transactionAmount = txn.transactionAmount,
          merchantCategory = txn.merchantCategory
        )
      )
    else None
  }
  // ==================================================
  // RULE 6 — HIGH Value Transaction (> $50,000)
  // ==================================================
  private def highAmountRule(txn: BankTransaction): Option[BankFraudAlert] = {
    if (txn.transactionAmount > 100000)
      Some(
        BankFraudAlert(
          customerId = txn.customerId,
          transactionId = txn.transactionId,
          reason = s"High value transaction: ${txn.transactionAmount}",
          transactionAmount = txn.transactionAmount,
          merchantCategory = txn.merchantCategory
        )
      )
    else None
  }

  // ==================================================
  // RULE 7 - NEGATIVE ACCOUNT BALANCE
  // ==================================================
  private def negativeAccountBalance(txn: BankTransaction): Option[BankFraudAlert] = {
    if (txn.accountBalance < 0)
      Some(
        BankFraudAlert(
          customerId = txn.customerId,
          transactionId = txn.transactionId,
          reason = s"Negative account balance: ${txn.accountBalance}",
          transactionAmount = txn.transactionAmount,
          merchantCategory = txn.merchantCategory
        )
      )
    else None
  }

  // ==================================================
  // RULE 8 - LOCATION MISMATCH
  // ==================================================
  private def locationMismatch(txn: BankTransaction): Option[BankFraudAlert] = {
    if (txn.accountBalance < 0)
      Some(
        BankFraudAlert(
          customerId = txn.customerId,
          transactionId = txn.transactionId,
          reason = s"Location mismatch: ${txn.transactionLocation} vs ${txn.city}",
          transactionAmount = txn.transactionAmount,
          merchantCategory = txn.merchantCategory
        )
      )
    else None
  }
}
