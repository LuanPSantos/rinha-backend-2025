package traineecodeplays.payment_proxy.payment.usecase

import org.springframework.stereotype.Service
import traineecodeplays.payment_proxy.payment.model.PaymentRequest
import traineecodeplays.payment_proxy.payment.producer.PaymentRequestProducer

@Service
class PlacePaymentRequestUseCase(

    private val repository: PaymentRequestProducer
) {

    suspend fun execute(paymentRequest: PaymentRequest) {

        repository.savePending(paymentRequest)
    }
}