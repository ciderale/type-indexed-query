package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.OrderingDirection
import org.jooq.Field

fun <T> Field<T>.ascending(mode: OrderingDirection) =
    when (mode) {
        OrderingDirection.ASC -> asc()
        OrderingDirection.DESC -> desc()
    }

fun <T> Field<T>.descending(mode: OrderingDirection) =
    when (mode) {
        OrderingDirection.ASC -> desc()
        OrderingDirection.DESC -> asc()
    }
