package traineecodeplays.payment_proxy

import java.math.BigDecimal

data class Transaction(
    val default: Data,
    val fallback: Data
) {
    data class Data(
        val totalRequests: Long,
        val totalAmount: BigDecimal
    )
}
