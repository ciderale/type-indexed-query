package io.github.ciderale.tiq.core

interface Projection<Q, T>

interface Ordering<Q>

enum class OrderingDirection { ASC, DESC }

interface Fetcher<T, R>

data class QuerySpec<Q : Any, T, R>(
    val query: Q,
    val ordering: Ordering<Q>?,
    val direction: OrderingDirection = OrderingDirection.ASC,
    val projection: Projection<Q, T>,
    val fetcher: Fetcher<T, R>,
)
