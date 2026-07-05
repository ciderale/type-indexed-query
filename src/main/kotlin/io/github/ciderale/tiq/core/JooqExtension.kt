package io.github.ciderale.tiq.core

import org.jooq.Condition
import org.jooq.DSLContext

fun <T, R> DSLContext.fetch(
    condition: Condition,
    projection: Projection<T>,
    mode: ResultMode<T, R>,
    registry: SelectMappingRegistry,
): R {
    val pair = registry[projection]
    return pair.fetch(mode, condition, this)
}
