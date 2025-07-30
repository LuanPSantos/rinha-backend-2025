package traineecodeplays.payment_proxy.payment.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.reactor.mono
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Service
import reactor.core.Disposable
import traineecodeplays.payment_proxy.payment.model.PaymentRequest
import traineecodeplays.payment_proxy.payment.producer.PaymentRequestProducer.Companion.PENDING
import traineecodeplays.payment_proxy.payment.usecase.ProcessPaymentUseCase
import java.util.UUID.*

@Service
class PaymentStreamConsumer(
    private val receiver: StreamReceiver<String, MapRecord<String, String, String>>,
    private val objectMapper: ObjectMapper,
    private val useCase: ProcessPaymentUseCase
) {
    private val group = "payment-group"
    private lateinit var subscription: Disposable

    @PostConstruct
    fun start() {
        subscription = receiver.receiveAutoAck(
            Consumer.from(group, randomUUID().toString()),
            StreamOffset.create(PENDING, ReadOffset.lastConsumed())
        ).flatMap { record ->
            val json = record.value["payload"]

            val payment = objectMapper.readValue(json, PaymentRequest::class.java)
            processPayment(payment)
        }.subscribe()
    }

    @PreDestroy
    fun stop() {
        if(!subscription.isDisposed) subscription.dispose()
    }

    private fun processPayment(payment: PaymentRequest) = mono {
        useCase.execute(payment)
    }
}