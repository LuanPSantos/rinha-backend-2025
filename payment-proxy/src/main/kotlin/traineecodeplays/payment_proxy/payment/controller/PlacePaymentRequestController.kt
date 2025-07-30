package traineecodeplays.payment_proxy.payment.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import traineecodeplays.payment_proxy.payment.usecase.PlacePaymentRequestUseCase
import traineecodeplays.payment_proxy.payment.model.PaymentRequest

@RestController
class PlacePaymentRequestController(
    private val useCase: PlacePaymentRequestUseCase
) {

    @PostMapping("/payments")
    suspend fun doPay(@RequestBody paymentRequest: PaymentRequest) {
        useCase.execute(paymentRequest)
    }
}