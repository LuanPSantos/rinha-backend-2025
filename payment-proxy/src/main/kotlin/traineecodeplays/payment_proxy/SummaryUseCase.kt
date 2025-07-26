package traineecodeplays.payment_proxy

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SummaryUseCase(
    private val repository: TransactionRepository
) {

    suspend fun execute(from: Instant?, to: Instant?): Transaction {

        val default =  repository.getDefault(from, to).reduce(Transaction.Data()) { acc, curr -> acc.plus(curr.amount) }.awaitSingle()
        val fallback =  repository.getFallback(from, to).reduce(Transaction.Data()) { acc, curr -> acc.plus(curr.amount) }.awaitSingle()

        return Transaction(default, fallback)
    }
}