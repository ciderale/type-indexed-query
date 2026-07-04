package io.github.ciderale.tiq.sample.domain

data class UserSummary(
    val id: String,
    val name: String,
)

data class UserDetail(
    val id: String,
    val name: String,
    val email: String,
)

data class OrderSummary(
    val x: String,
)

data class PagedList<T>(
    val items: List<T>,
    val offset: Int,
    val total: Int,
)
