package schema

case class BankTransaction(
                            customerId: String,
                            customerName: String,
                            gender: String,
                            age: Int,
                            state: String,
                            city: String,
                            bankBranch: String,
                            accountType: String,
                            transactionId: String,
                            transactionDate: String,
                            transactionTime: String,
                            transactionAmount: Double,
                            merchantId: String,
                            transactionType: String,
                            merchantCategory: String,
                            accountBalance: Double,
                            transactionDevice: String,
                            transactionLocation: String,
                            deviceType: String,
                            isFraud: Int,
                            transactionCurrency: String,
                            customerContact: String,
                            transactionDescription: String,
                            customerEmail: String
                          )

