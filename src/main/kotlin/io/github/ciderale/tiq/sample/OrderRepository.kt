package io.github.ciderale.tiq.sample

import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.core.experimental.FetchSpec
import io.github.ciderale.tiq.sample.domain.OrderSummary

interface OrderRepository {
    data class Query(
        val openOnly: Boolean = false,
    )

    object Summary : Projection<OrderSummary>

    fun <T, R> fetch(
        query: Query,
        spec: FetchSpec<OrderSummary, R, Summary>,
    ): R
}
