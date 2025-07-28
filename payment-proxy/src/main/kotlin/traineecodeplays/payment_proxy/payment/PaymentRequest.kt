package traineecodeplays.payment_proxy.payment

import java.math.BigDecimal
import java.time.*
import java.time.format.DateTimeFormatter

data class PaymentRequest(
    val correlationId: String,
    val amount: BigDecimal,
    val requestedAt: String = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
        .withZone(ZoneOffset.UTC).format(Instant.now())
)