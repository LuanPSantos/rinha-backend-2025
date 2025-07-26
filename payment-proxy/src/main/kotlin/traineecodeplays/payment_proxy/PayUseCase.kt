package traineecodeplays.payment_proxy

import org.springframework.stereotype.Service
import traineecodeplays.payment_proxy.PaymentProcessorClient.Client.*

@Service
class PayUseCase(
    private val client: PaymentProcessorClient,
    private val repository: TransactionRepository
) {

    suspend fun execute(paymentRequest: PaymentRequest) {
        val executedBy = client.pay(paymentRequest)

        when(executedBy) {
            DEFAULT -> repository.saveDefault(paymentRequest)
            FALLBACK -> repository.saveFallback(paymentRequest)
        }
    }
}