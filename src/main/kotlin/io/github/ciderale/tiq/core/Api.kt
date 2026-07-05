package io.github.ciderale.tiq.core

interface Projection<T>

interface Fetcher<T, R>

data class QuerySpec<Q, T, R>(
    val query: Q,
    val projection: Projection<T>,
    val mode: Fetcher<T, R>,
)
