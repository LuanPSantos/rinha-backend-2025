package traineecodeplays.payment_proxy.payment

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelOption
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime


@Component
class PaymentProcessorClient(
    @Value("\${payment-processor.default.url}") defaultUrl: String,
    @Value("\${payment-processor.fallback.url}") fallback: String,
    @Value("\${web-client.pool-size}") poolSize: Int,
    val mapper: ObjectMapper
) {
    private val default = webClient(defaultUrl, poolSize)
    private val fallback = webClient(fallback, poolSize)

    suspend fun pay(paymentRequest: PaymentRequest): Client {
        return default
            .post()
            .uri("/payments")
            .bodyValue(paymentRequest)
            .exchangeToMono {
                if(it.statusCode().isError) {
                    callFallback(paymentRequest).map { Client.FALLBACK }
                }else{
                    Mono.just(Client.DEFAULT)
                }

            }.awaitSingle()
    }

    fun callFallback(paymentRequest: PaymentRequest): Mono<*> {
        return fallback
            .post()
            .uri("/payments")
            .bodyValue(paymentRequest)
            .retrieve()
            .onStatus({ it.isError }) {
                Mono.error(Exception("Failed to process request"))
            }
            .toBodilessEntity()
    }

    private final fun webClient(baseUrl: String, poolSize: Int): WebClient {
        val provider = ConnectionProvider.builder("custom")
            .maxConnections(poolSize)
            .pendingAcquireTimeout(Duration.ofSeconds(1))
            .build()

        val httpClient: HttpClient = HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
            .responseTimeout(Duration.ofSeconds(1))
        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

    enum class Client {
        DEFAULT, FALLBACK
    }
}