package traineecodeplays.payment_proxy

import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.setAndAwait
import org.springframework.stereotype.Component

@Component
class TransactionRepository(
    private val redis: ReactiveRedisOperations<String, Transaction>
) {

    suspend fun save(transaction: Transaction) {
        redis.opsForValue().setAndAwait(KEY, transaction)
    }

    suspend fun get(): Transaction? {
        return redis.opsForValue().getAndAwait(KEY)
    }

    private companion object {
        const val KEY = "transaction"
    }
}