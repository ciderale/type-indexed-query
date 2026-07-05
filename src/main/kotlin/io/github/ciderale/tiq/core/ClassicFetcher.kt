package io.github.ciderale.tiq.core

data class PagedList<T>(
    val items: List<T>,
    val offset: Int,
    val total: Int,
)

// This is important to dependently determine the return type
sealed interface ClassicFetcher<T, R> : Fetcher<T, R> {
    class Count<T> : Fetcher<T, Int>

    class One<T> : Fetcher<T, T>

    class Many<T> : Fetcher<T, List<T>>

    data class Paged<T>(
        val offset: Int,
        val limit: Int,
    ) : Fetcher<T, PagedList<T>>
}
