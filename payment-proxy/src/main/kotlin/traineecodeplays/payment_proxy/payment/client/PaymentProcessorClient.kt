package traineecodeplays.payment_proxy.payment.client

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutException
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import traineecodeplays.payment_proxy.payment.model.PaymentRequest
import java.time.Duration

@Component
class PaymentProcessorClient(
    @Value("\${payment-processor.default.url}") defaultUrl: String,
    @Value("\${payment-processor.fallback.url}") fallback: String,
    @Value("\${web-client.pool-size}") poolSize: Int,
    @Value("\${web-client.pending-acquire-max-count}") pendingAcquireMaxCount: Int,
    @Value("\${web-client.timeout}") timeout: Long
) {
    private val default = webClient(defaultUrl, poolSize, pendingAcquireMaxCount, timeout)
    private val fallback = webClient(fallback, poolSize, pendingAcquireMaxCount, timeout)

    suspend fun processPayment(paymentRequest: PaymentRequest): Client {
        return default
            .post()
            .uri("/payments")
            .bodyValue(paymentRequest)
            .exchangeToMono {
                if(it.statusCode().isError) {
                    callFallback(paymentRequest)
                }else{
                    Mono.just(Client.DEFAULT)
                }
            }.onErrorResume(WebClientRequestException::class.java) {
                Mono.just(Client.NONE)
            }.awaitSingle()
    }

    fun callFallback(paymentRequest: PaymentRequest): Mono<Client> {
        return fallback
            .post()
            .uri("/payments")
            .bodyValue(paymentRequest)
            .exchangeToMono {
                if(it.statusCode().isError) {
                    Mono.just(Client.NONE)
                }else{
                    Mono.just(Client.FALLBACK)
                }
            }.onErrorResume(WebClientRequestException::class.java) {
                Mono.just(Client.NONE)
            }
    }

    private final fun webClient(baseUrl: String, poolSize: Int, pendingAcquireMaxCount: Int, timeout: Long): WebClient {
        val provider = ConnectionProvider.builder("custom")
            .maxConnections(poolSize)
            .pendingAcquireTimeout(Duration.ofSeconds(30))
            .pendingAcquireMaxCount(pendingAcquireMaxCount)
            .build()

        val httpClient: HttpClient = HttpClient.create(provider)
            .responseTimeout(Duration.ofMillis(timeout))

        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

    enum class Client {
        DEFAULT, FALLBACK, NONE
    }
}