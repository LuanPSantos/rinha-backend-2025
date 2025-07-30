package traineecodeplays.payment_proxy.summary.model

import java.math.BigDecimal

data class Summary(
    val default: Data = Data(),
    val fallback: Data = Data()
) {
    data class Data(
        val totalRequests: Long = 0,
        val totalAmount: BigDecimal = BigDecimal.ZERO
    ) {
        fun plus(amount: BigDecimal): Data {
            return this.copy(totalRequests = this.totalRequests + 1, totalAmount = this.totalAmount.plus(amount))
        }
    }
}
