package io.github.ciderale.tiq.core

interface Projection<Q, T>

interface Fetcher<T, R>

data class QuerySpec<Q, T, R>(
    val query: Q,
    val projection: Projection<Q, T>,
    val fetcher: Fetcher<T, R>,
)
