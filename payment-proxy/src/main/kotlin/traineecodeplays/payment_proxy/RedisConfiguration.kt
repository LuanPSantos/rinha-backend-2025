package traineecodeplays.payment_proxy

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.newSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfiguration(
    @Value("\${redis.host}") private val redisHost: String,
    @Value("\${redis.port}") private val redisPort: Int
) {

    @Bean
    fun reactiveRedisTemplate(
        factory: ReactiveRedisConnectionFactory,
        objectMapper: ObjectMapper
    ): ReactiveRedisTemplate<String, PaymentRequest> {
        val keySerializer = StringRedisSerializer()
        val valueSerializer = Jackson2JsonRedisSerializer(objectMapper, PaymentRequest::class.java)
        val builder = newSerializationContext<String, PaymentRequest>(keySerializer)
        val context = builder.value(valueSerializer).build()
        return ReactiveRedisTemplate(factory, context)
    }

    @Bean
    @Primary
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {
        return LettuceConnectionFactory(redisHost, redisPort)
    }
}