package io.github.ciderale.tiq.core

import org.jooq.Condition
import org.jooq.DSLContext

fun <T, R> DSLContext.fetch(
    condition: Condition,
    spec: FetchSpec<T, R, Projection<T>>,
    registry: SelectMappingRegistry,
): R {
    val pair = registry[spec.projection]
    return pair.fetch(spec.mode, condition, this)
}
