package io.github.ciderale.tiq.core.jooq

import io.github.ciderale.tiq.core.OrderingDirection
import io.github.ciderale.tiq.core.QuerySpec
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectJoinStep
import org.jooq.SelectSeekStepN
import org.jooq.SortField

interface JooqQueryTranslator {
    fun <Q, T, R> translate(spec: QuerySpec<Q, T, R>): JooqQueryComponents<*, T, R>
}

data class JooqQueryComponents<X : Record, T, R>(
    val condition: Condition,
    val select: Selector<X>,
    val ordering: List<SortFieldFactory>,
    val direction: OrderingDirection,
    val mapper: Mapper<X, T>,
    val fetch: Fetcher<X, T, R>,
) {
    typealias Selector<X> = (DSLContext) -> SelectJoinStep<X>
    typealias SortFieldFactory = (OrderingDirection) -> SortField<*>
    typealias Mapper<X, T> = (X) -> T
    typealias Fetcher<X, T, R> = (DSLContext, SelectSeekStepN<X>, (X) -> T) -> R

    fun execute(ctx: DSLContext): R {
        val ordering = ordering.map { it(direction) }
        val sqlQuery = select(ctx).where(condition).orderBy(ordering)
        return fetch(ctx, sqlQuery, mapper)
    }
}

fun <Q, T, R> DSLContext.fetch(
    spec: QuerySpec<Q, T, R>,
    translator: JooqQueryTranslator,
) = translator.translate(spec).execute(this)
