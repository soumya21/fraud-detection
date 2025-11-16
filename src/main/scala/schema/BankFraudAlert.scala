package schema

case class BankFraudAlert(
                       customerId: String,
                       transactionId: String,
                       reason: String,
                       transactionAmount: Double,
                       merchantCategory: String
                     )
