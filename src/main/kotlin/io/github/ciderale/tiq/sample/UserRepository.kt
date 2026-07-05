package io.github.ciderale.tiq.sample

import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.sample.domain.UserDetail
import io.github.ciderale.tiq.sample.domain.UserSummary

interface UserRepository {
    data class Query(
        val id: String? = null,
        val activeOnly: Boolean? = null,
    )

    // Projections belong to this Query scope
    sealed interface UP<T> : Projection<T>

    data object Summary : UP<UserSummary>

    data object Detail : UP<UserDetail>

    fun <T, R> fetch(
        query: Query,
        mode: Fetcher<T, R>,
        projection: UP<T>,
    ): R
}
