package io.github.ciderale.tiq.core

import io.github.ciderale.tiq.sample.domain.PagedList

interface Projection<T>

// This is important to dependently determine the return type
sealed interface ResultMode<T, R> {
    class Count<T> : ResultMode<T, Int>

    class One<T> : ResultMode<T, T>

    class Many<T> : ResultMode<T, List<T>>

    data class Paged<T>(
        val offset: Int,
        val limit: Int,
    ) : ResultMode<T, PagedList<T>>
}

data class QuerySpec<Q, T, R>(
    val query: Q,
    val projection: Projection<T>,
    val mode: ResultMode<T, R>,
)
