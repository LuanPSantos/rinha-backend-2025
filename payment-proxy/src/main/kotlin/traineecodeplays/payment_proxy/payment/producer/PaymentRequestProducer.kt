package traineecodeplays.payment_proxy.payment.producer

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Range
import org.springframework.data.domain.Range.Bound
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.addAndAwait
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import traineecodeplays.payment_proxy.payment.model.PaymentRequest
import java.time.Instant

@Component
class PaymentRequestProducer(
    private val redis: ReactiveRedisTemplate<String, PaymentRequest>,
    val objectMapper: ObjectMapper
) {
    suspend fun savePending(data: PaymentRequest) {
        val record = StreamRecords.newRecord()
            .`in`(PENDING)
            .ofObject(objectMapper.writeValueAsString(data))

        redis.opsForStream<String, String>().add(record).awaitSingle()
    }

    companion object {
        const val PENDING = "pending"
    }
}