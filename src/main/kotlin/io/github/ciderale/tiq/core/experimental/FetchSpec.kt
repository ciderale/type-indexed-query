package io.github.ciderale.tiq.core.experimental

import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.core.ResultMode
import io.github.ciderale.tiq.sample.domain.Mode

data class FetchSpec<T, R, out P : Projection<T>>(
    val projection: P,
    val mode: ResultMode<T, R>,
)

// FetchSpec factory methods
fun <T, P : Projection<T>> Mode.Count.of(p: P) = FetchSpec(p, ResultMode.Count())

fun <T, P : Projection<T>> Mode.One.of(p: P) = FetchSpec(p, ResultMode.One())

fun <T, P : Projection<T>> Mode.Many.of(p: P) = FetchSpec(p, ResultMode.Many())

fun <T, P : Projection<T>> Mode.Paged.of(p: P) = FetchSpec(p, ResultMode.Paged(offset, limit))

// unclear if starting from Mode or Projection is more convenient
fun <T, P : Projection<T>> P.count() = FetchSpec(this, ResultMode.Count())

fun <T, P : Projection<T>> P.one() = FetchSpec(this, ResultMode.One())

fun <T, P : Projection<T>> P.many() = FetchSpec(this, ResultMode.Many())

fun <T, P : Projection<T>> P.paged(
    offset: Int,
    limit: Int,
) = FetchSpec(this, ResultMode.Paged(offset, limit))
