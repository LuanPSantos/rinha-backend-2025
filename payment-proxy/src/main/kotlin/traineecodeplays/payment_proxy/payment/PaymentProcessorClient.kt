package traineecodeplays.payment_proxy.payment

import io.netty.channel.ChannelOption
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Component
class PaymentProcessorClient(
    @Value("\${payment-processor.default.url}") defaultUrl: String,
    @Value("\${payment-processor.fallback.url}") fallback: String,
    @Value("\${web-client.pool-size}") poolSize: Int,
    @Value("\${web-client.pending-acquire-max-count}") pendingAcquireMaxCount: Int
) {
    private val default = webClient(defaultUrl, poolSize, pendingAcquireMaxCount)
    private val fallback = webClient(fallback, poolSize, pendingAcquireMaxCount)

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
                println("fallback status is ${it.statusCode().value()}")

                Mono.error(Exception("Failed to process request"))
            }
            .toBodilessEntity()
    }

    private final fun webClient(baseUrl: String, poolSize: Int, pendingAcquireMaxCount: Int): WebClient {
        val provider = ConnectionProvider.builder("custom")
            .maxConnections(poolSize)
            .pendingAcquireTimeout(Duration.ofSeconds(2))
            .pendingAcquireMaxCount(pendingAcquireMaxCount)
            .build()

        val httpClient: HttpClient = HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
            .responseTimeout(Duration.ofSeconds(2))

        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

    enum class Client {
        DEFAULT, FALLBACK
    }
}