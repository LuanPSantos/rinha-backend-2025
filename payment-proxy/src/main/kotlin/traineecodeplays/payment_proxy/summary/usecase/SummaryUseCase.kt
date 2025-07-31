package traineecodeplays.payment_proxy.summary.usecase

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import traineecodeplays.payment_proxy.payment.repository.PaymentRequestRepository
import traineecodeplays.payment_proxy.summary.model.Summary
import java.time.Instant

@Service
class SummaryUseCase(
    private val repository: PaymentRequestRepository
) {

    suspend fun execute(from: Instant?, to: Instant?): Summary {

        val default = repository
            .getDefault(from, to)
            .reduce(Summary.Data()) { acc, curr ->
                acc.plus(curr.amount)
            }.awaitSingle()

        val fallback = repository
            .getFallback(from, to)
            .reduce(Summary.Data()) { acc, curr ->
                acc.plus(curr.amount)
            }.awaitSingle()

        val none = repository
            .getNone(from, to)
            .reduce(Summary.Data()) { acc, curr ->
                acc.plus(curr.amount)
            }.awaitSingle()

        return Summary(default, fallback, none)
    }
}