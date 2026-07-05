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

sealed interface Mode {
    object Count : Mode

    object One : Mode

    object Many : Mode

    data class Paged(
        val offset: Int,
        val limit: Int,
    ) : Mode
}
