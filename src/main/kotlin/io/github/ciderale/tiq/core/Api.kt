package io.github.ciderale.tiq.core

interface Projection<Q, T>

interface Ordering<Q>

interface Fetcher<T, R>

data class QuerySpec<Q, T, R>(
    val query: Q,
    val ordering: Ordering<Q>?,
    val projection: Projection<Q, T>,
    val fetcher: Fetcher<T, R>,
)
