package traineecodeplays.payment_proxy.summary

import org.springframework.data.domain.Range
import org.springframework.data.domain.Range.Bound
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.addAndAwait
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.time.Instant

@Component
class TransactionRepository(
    private val redis: ReactiveRedisTemplate<String, BigDecimal>
) {
    suspend fun saveDefault(amount: BigDecimal) {
        redis.opsForZSet().addAndAwait(DEFAULT, amount, Instant.now().toEpochMilli().toDouble())
    }

    suspend fun saveFallback(amount: BigDecimal) {
        redis.opsForZSet().addAndAwait(FALLBACK, amount, Instant.now().toEpochMilli().toDouble())
    }

    suspend fun getDefault(from: Instant?, to: Instant?): Flux<BigDecimal> {
        return get(DEFAULT, from, to)
    }

    suspend fun getFallback(from: Instant?, to: Instant?) : Flux<BigDecimal> {
        return get(FALLBACK, from, to)
    }

    suspend fun get(client: String, from: Instant?, to: Instant?) : Flux<BigDecimal> {
        if(from != null && to != null) {
            return redis.opsForZSet().rangeByScore(client, Range.open(from.toEpochMilli().toDouble(), to.toEpochMilli().toDouble()))
        }
        if(from != null) {
            return redis.opsForZSet().rangeByScore(client, Range.of(Bound.inclusive(from.toEpochMilli().toDouble()), Bound.unbounded()))
        }

        return redis.opsForZSet().rangeByScore(client, Range.unbounded())
    }

    private companion object {
        const val DEFAULT = "default"
        const val FALLBACK = "fallback"
    }
}