package io.github.ciderale.tiq.sample

import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.sample.domain.OrderSummary

interface OrderRepository {
    data class Query(
        val openOnly: Boolean = false,
    )

    object Summary : Projection<OrderSummary>

    fun <T, R> fetch(
        query: Query,
        projection: Summary,
        mode: Fetcher<T, R>,
    ): R
}
