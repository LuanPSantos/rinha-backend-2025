package traineecodeplays.payment_proxy.payment.usecase

import org.springframework.stereotype.Service
import traineecodeplays.payment_proxy.payment.client.PaymentProcessorClient
import traineecodeplays.payment_proxy.payment.client.PaymentProcessorClient.Client.DEFAULT
import traineecodeplays.payment_proxy.payment.client.PaymentProcessorClient.Client.FALLBACK
import traineecodeplays.payment_proxy.payment.model.PaymentRequest
import traineecodeplays.payment_proxy.payment.producer.PaymentRequestProducer
import traineecodeplays.payment_proxy.payment.repository.PaymentRequestRepository

@Service
class ProcessPaymentUseCase(
    private val repository: PaymentRequestRepository,
    private val producer: PaymentRequestProducer,
    private val client: PaymentProcessorClient,
) {
    suspend fun execute(payment: PaymentRequest) {
        val executedBy = client.processPayment(payment)

        when (executedBy) {
            DEFAULT -> repository.saveDefault(payment)
            FALLBACK -> repository.saveFallback(payment)
            else -> repository.saveNone(payment)
        }
    }
}