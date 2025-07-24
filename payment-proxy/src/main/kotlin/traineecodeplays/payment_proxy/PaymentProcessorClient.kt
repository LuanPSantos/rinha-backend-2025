package traineecodeplays.payment_proxy

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class PaymentProcessorClient(
    builder: WebClient.Builder,
    @Value("\${payment-processor.default.url}") defaultUrl: String,
    @Value("\${payment-processor.fallback.url}") fallback: String,
) {
    private val default = builder.baseUrl(defaultUrl).build()
    private val fallback = builder.baseUrl(fallback).build()

    suspend fun pay(paymentRequest: PaymentRequest): Client {
        return default
            .post()
            .bodyValue(paymentRequest)
            .exchangeToMono {
                if(it.statusCode().is5xxServerError) callFallback(paymentRequest).map { Client.FALLBACK }

                Mono.just(Client.DEFAULT)
            }.awaitSingle()
    }

    fun callFallback(paymentRequest: PaymentRequest): Mono<*> {
        return fallback
            .post()
            .bodyValue(paymentRequest)
            .retrieve()
            .toBodilessEntity()
    }

    enum class Client {
        DEFAULT, FALLBACK
    }
}