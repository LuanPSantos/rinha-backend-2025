package traineecodeplays.payment_proxy.payment

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactor.mono
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import traineecodeplays.payment_proxy.summary.TransactionRepository

@Service
class PaymentStreamConsumer(
    private val redis: ReactiveRedisTemplate<String, PaymentRequest>,
    private val repository: TransactionRepository,
    private val client: PaymentProcessorClient
) {

    @PostConstruct
    fun startSubscription() {
        redis.listenToChannel(TransactionRepository.PENDING)
            .flatMap { msg ->
                processPayment(msg.message)
            }
            .onErrorContinue { e, _ ->
                println("Error processing message " + e.printStackTrace())
            }
            .subscribe()
    }

    private fun processPayment(payment: PaymentRequest): Mono<*> {
        return mono {
            val executedBy = client.pay(payment)

            when(executedBy) {
                PaymentProcessorClient.Client.DEFAULT -> repository.saveDefault(payment)
                PaymentProcessorClient.Client.FALLBACK -> repository.saveFallback(payment)
            }
        }
    }
}