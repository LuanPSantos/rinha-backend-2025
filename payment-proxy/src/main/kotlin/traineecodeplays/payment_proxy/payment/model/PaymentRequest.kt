package traineecodeplays.payment_proxy.payment.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.*
import java.time.format.DateTimeFormatter

data class PaymentRequest(
    val correlationId: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    val requestedAt: Instant = Instant.now()
)