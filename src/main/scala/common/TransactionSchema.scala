package common

case class Transaction(
                        transactionId: String,
                        userId: String,
                        amount: Double,
                        location: String,
                        timestamp: Long
                      )
