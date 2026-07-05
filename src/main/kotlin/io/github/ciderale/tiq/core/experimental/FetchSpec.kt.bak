package io.github.ciderale.tiq.core.experimental

import io.github.ciderale.tiq.core.ClassicFetcher
import io.github.ciderale.tiq.core.Fetcher
import io.github.ciderale.tiq.core.Projection
import io.github.ciderale.tiq.sample.domain.Mode

data class FetchSpec<T, R, out P : Projection<T>>(
    val projection: P,
    val mode: Fetcher<T, R>,
)

// FetchSpec factory methods
fun <T, P : Projection<T>> Mode.Count.of(p: P) = FetchSpec(p, ClassicFetcher.Count())

fun <T, P : Projection<T>> Mode.One.of(p: P) = FetchSpec(p, ClassicFetcher.One())

fun <T, P : Projection<T>> Mode.Many.of(p: P) = FetchSpec(p, ClassicFetcher.Many())

fun <T, P : Projection<T>> Mode.Paged.of(p: P) = FetchSpec(p, ClassicFetcher.Paged(offset, limit))

// unclear if starting from Mode or Projection is more convenient
fun <T, P : Projection<T>> P.count() = FetchSpec(this, ClassicFetcher.Count())

fun <T, P : Projection<T>> P.one() = FetchSpec(this, ClassicFetcher.One())

fun <T, P : Projection<T>> P.many() = FetchSpec(this, ClassicFetcher.Many())

fun <T, P : Projection<T>> P.paged(
    offset: Int,
    limit: Int,
) = FetchSpec(this, ClassicFetcher.Paged(offset, limit))
