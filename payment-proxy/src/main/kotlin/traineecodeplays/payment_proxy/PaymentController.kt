package traineecodeplays.payment_proxy

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentController(
    private val useCase: PayUseCase
) {

    @PostMapping("/payments")
    suspend fun doPay(@RequestBody paymentRequest: PaymentRequest) {
        useCase.execute(paymentRequest)
    }
}