package io.github.ciderale.tiq.sample

import io.github.ciderale.tiq.core.ClassicFetcher
import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.Ordering
import io.github.ciderale.tiq.core.OrderingDirection
import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.sample.domain.UserDetail
import io.github.ciderale.tiq.sample.domain.UserSummary

interface UserRepository {
    data class Query(
        val id: String? = null,
        val activeOnly: Boolean? = null,
    )

    enum class OrderBy : Ordering<Query> {
        ID,
        NAME,
    }

    // Projections belong to this Query scope
    sealed interface UP<T> : Projection<Query, T>

    data object Summary : UP<UserSummary>

    data object Detail : UP<UserDetail>

    fun <T, R> fetch(
        query: Query,
        projection: UP<T>,
        fetcher: Fetcher<T, R>,
        ordering: OrderBy = OrderBy.NAME,
        direction: OrderingDirection = OrderingDirection.ASC,
    ): R

    fun <T> get(
        id: String,
        projection: UP<T>,
    ): T = fetch(Query(id = id), projection, ClassicFetcher.One())
}
