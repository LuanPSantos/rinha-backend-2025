package traineecodeplays.payment_proxy.payment

import org.springframework.stereotype.Service
import traineecodeplays.payment_proxy.summary.TransactionRepository
import traineecodeplays.payment_proxy.payment.PaymentProcessorClient.Client.*

@Service
class PayUseCase(
    private val client: PaymentProcessorClient,
    private val repository: TransactionRepository
) {

    suspend fun execute(paymentRequest: PaymentRequest) {
        val executedBy = client.pay(paymentRequest)

        when(executedBy) {
            DEFAULT -> repository.saveDefault(paymentRequest.amount)
            FALLBACK -> repository.saveFallback(paymentRequest.amount)
        }
    }
}