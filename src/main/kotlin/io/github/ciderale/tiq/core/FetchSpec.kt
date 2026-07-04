package io.github.ciderale.tiq.core

import io.github.ciderale.tiq.sample.domain.PagedList

data class FetchSpec<T, R, P : Projection<T>>(
    val projection: P,
    val mode: ResultMode<T, R>,
)

interface Projection<T>

// This is important to dependently determine the return type
sealed interface ResultMode<T, R> {
    class One<T> : ResultMode<T, T>

    class Many<T> : ResultMode<T, List<T>>

    data class Paged<T>(
        val offset: Int,
        val limit: Int,
    ) : ResultMode<T, PagedList<T>>
}

sealed interface Mode {
    object One : Mode

    object Many : Mode

    data class Paged(
        val offset: Int,
        val limit: Int,
    ) : Mode
}

// FetchSpec factory methods
fun <T, P : Projection<T>> Mode.One.of(p: P) = FetchSpec(p, ResultMode.One())

fun <T, P : Projection<T>> Mode.Many.of(p: P) = FetchSpec(p, ResultMode.Many())

fun <T, P : Projection<T>> Mode.Paged.of(p: P) = FetchSpec(p, ResultMode.Paged(offset, limit))

// unclear if starting from Mode or Projection is more convenient
fun <T, P : Projection<T>> P.one() = FetchSpec(this, ResultMode.One<T>())

fun <T, P : Projection<T>> P.many() = FetchSpec(this, ResultMode.Many<T>())

fun <T, P : Projection<T>> P.paged(
    offset: Int,
    limit: Int,
) = FetchSpec(this, ResultMode.Paged<T>(offset, limit))
