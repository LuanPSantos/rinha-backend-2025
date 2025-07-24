package traineecodeplays.payment_proxy

import java.math.BigDecimal
import java.time.Instant

data class PaymentRequest(
    val correlationId: String,
    val amount: BigDecimal,
    val requestedAt: Instant = Instant.now()
)