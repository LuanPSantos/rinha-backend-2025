package traineecodeplays.payment_proxy.summary.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import traineecodeplays.payment_proxy.summary.usecase.SummaryUseCase
import traineecodeplays.payment_proxy.summary.model.Summary
import java.time.Instant

@RestController
class SummaryController(
    private val useCase: SummaryUseCase
) {

    @GetMapping("/payments-summary")
    suspend fun getSummary(
        @RequestParam("from") from: Instant? = null,
        @RequestParam("to") to: Instant? = null
    ): Summary {
        return useCase.execute(from, to)
    }
}