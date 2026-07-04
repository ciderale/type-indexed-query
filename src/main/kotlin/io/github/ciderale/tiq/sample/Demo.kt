package io.github.ciderale.tiq.sample

import io.github.ciderale.tiq.core.FetchSpec
import io.github.ciderale.tiq.core.Mode
import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.core.many
import io.github.ciderale.tiq.core.of
import io.github.ciderale.tiq.sample.domain.OrderSummary
import io.github.ciderale.tiq.sample.domain.PagedList
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
        spec: FetchSpec<T, R, UP<T>>,
    ): R
}

// -------------------------
// Example domain: OrderRepository
// -------------------------

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

fun test(
    userRepo: UserRepository,
    orderRepo: OrderRepository,
) {
    val users1: UserSummary =
        userRepo.fetch(
            UserRepository.Query(activeOnly = true),
            Mode.One.of(UserRepository.Summary),
//            UserRepository.Summary.one()
        )

    val users2: List<UserDetail> =
        userRepo.fetch(
            UserRepository.Query(),
            UserRepository.Detail.many(),
        )

    val page: PagedList<UserSummary> =
        userRepo.fetch(
            UserRepository.Query(activeOnly = true),
            Mode.Paged(0, 20).of(UserRepository.Summary),
//            UserRepository.Summary.paged(offset = 0, limit = 50)
        )
}
