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
            DEFAULT -> updateDefault(paymentRequest)
            FALLBACK -> updateFallback(paymentRequest)
        }
    }

    private suspend fun updateFallback(paymentRequest: PaymentRequest) {
        val transaction = repository.get()

        transaction?.fallback?.totalAmount?.plus(paymentRequest.amount)?.let {
            repository.save(transaction)
        }

        println(transaction)
    }

    private suspend fun updateDefault(paymentRequest: PaymentRequest) {
        val transaction = repository.get()

        transaction?.default?.totalAmount?.plus(paymentRequest.amount)?.let {
            repository.save(transaction)
        }

        println(transaction)
    }
}