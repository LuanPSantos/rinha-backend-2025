package traineecodeplays.payment_proxy.payment.repository

import org.springframework.data.domain.Range
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.addAndAwait
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import traineecodeplays.payment_proxy.payment.model.PaymentRequest
import java.time.Instant

@Component
class PaymentRequestRepository(
    private val redis: ReactiveRedisTemplate<String, PaymentRequest>
) {

    suspend fun saveDefault(data: PaymentRequest) {
        redis.opsForZSet().addAndAwait(DEFAULT, data, data.requestedAt.toEpochMilli().toDouble())
    }

    suspend fun saveNone(data: PaymentRequest) {
        redis.opsForZSet().addAndAwait(NONE, data, data.requestedAt.toEpochMilli().toDouble())
    }

    suspend fun saveFallback(data: PaymentRequest) {
        redis.opsForZSet().addAndAwait(FALLBACK, data, data.requestedAt.toEpochMilli().toDouble())
    }

    fun getDefault(from: Instant?, to: Instant?): Flux<PaymentRequest> {
        return get(DEFAULT, from, to)
    }

    fun getFallback(from: Instant?, to: Instant?) : Flux<PaymentRequest> {
        return get(FALLBACK, from, to)
    }

    fun getNone(from: Instant?, to: Instant?) : Flux<PaymentRequest> {
        return get(NONE, from, to)
    }

    fun get(client: String, from: Instant?, to: Instant?) : Flux<PaymentRequest> {

        if(from != null && to != null) {
            return redis.opsForZSet().rangeByScore(client, Range.open(from.toEpochMilli().toDouble(), to.toEpochMilli().toDouble()))
        }
        if(from != null) {
            return redis.opsForZSet().rangeByScore(client, Range.of(Range.Bound.inclusive(from.toEpochMilli().toDouble()), Range.Bound.unbounded()))
        }

        return redis.opsForZSet().rangeByScore(client, Range.unbounded())
    }

    companion object {
        const val DEFAULT = "default"
        const val FALLBACK = "fallback"
        const val NONE = "none"
    }
}