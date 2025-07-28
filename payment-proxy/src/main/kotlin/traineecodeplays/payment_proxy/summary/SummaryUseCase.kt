package traineecodeplays.payment_proxy.summary

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

        println("default is $default")
        println("fallback is $fallback")

        return Transaction(default, fallback)
    }
}